package pey.vpn.net.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.tbruyelle.rxpermissions3.RxPermissions
import pey.vpn.net.AppConfig
import pey.vpn.net.AppConfig.VPN
import pey.vpn.net.R
import pey.vpn.net.api.Model.ConfigsRequestBody
import pey.vpn.net.api.ViewModel.MainViewModelApi
import pey.vpn.net.databinding.ActivityMainBinding
import pey.vpn.net.dto.EConfigType
import pey.vpn.net.dto.ProfileItem
import pey.vpn.net.dto.ServersCache
import pey.vpn.net.extension.toast
import pey.vpn.net.handler.AngConfigManager
import pey.vpn.net.handler.MigrateManager
import pey.vpn.net.handler.MmkvManager
import pey.vpn.net.helper.SimpleItemTouchHelperCallback
import pey.vpn.net.service.V2RayServiceManager
import pey.vpn.net.util.Utils
import pey.vpn.net.viewmodel.MainViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.drakeet.support.toast.ToastCompat
import pey.vpn.net.fragments.configs.ConfigsFragment
import pey.vpn.net.fragments.home.HomeFragment
import pey.vpn.net.fragments.logs.LogsFragment
import pey.vpn.net.fragments.setting.SettingFragment
import pey.vpn.net.v2hub.dialogs.DeleteConfigFragment
import pey.vpn.net.v2hub.dialogs.GameModeInfoFragment
import pey.vpn.net.v2hub.utility.NetworkSpeedMonitor
import pey.vpn.net.v2hub.utility.VpnTimer
import pey.vpn.net.v2hub.utils.ConfigManager
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    val networkSpeedMonitor = NetworkSpeedMonitor()

    val vpnTimer = VpnTimer()

    lateinit var deleteConfigFragment: DeleteConfigFragment
    lateinit var gameModeInfoFragment: GameModeInfoFragment
    val viewModel: MainViewModelApi by viewModels()
    lateinit var configManager: ConfigManager

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val adapter by lazy { MainRecyclerAdapter(this) }
    val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startV2Ray()
            }
        }
    private val requestSubSettingActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            initGroupTab()
        }
    private val tabGroupListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            val selectId = tab?.tag.toString()
            if (selectId != mainViewModel.subscriptionId) {
                mainViewModel.subscriptionIdChanged(selectId)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }
    private var mItemTouchHelper: ItemTouchHelper? = null
    val mainViewModel: MainViewModel by viewModels()

    lateinit var homeFragment: HomeFragment
    lateinit var configsFragment: ConfigsFragment
    lateinit var logsFragment: LogsFragment
    lateinit var settingFragment: SettingFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = getString(R.string.title_server)
        setSupportActionBar(binding.toolbar)

        getFreeConfig()

        initFragments()
        setupBottomNav()
        bottomNavColorChange()

        binding.bottomNavLl.setOnClickListener {}


        binding.fab.setOnClickListener {
            //fabClick()
        }
        binding.layoutTest.setOnClickListener {
            //testPing()
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        mItemTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(adapter))
        mItemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        initGroupTab()
        setupViewModel()
        migrateLegacy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RxPermissions(this)
                .request(Manifest.permission.POST_NOTIFICATIONS)
                .subscribe {
                    if (!it)
                        toast(R.string.toast_permission_denied_notification)
                }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    //super.onBackPressed()
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        Log.i("erfannnnnnnnnnnnnnooooo", "onCreate: ${mainViewModel.serversCache}")

    }

    fun testPing() {

        if (mainViewModel.isRunning.value == true) {
            homeFragment.setTestState(getString(R.string.connection_test_testing))
            mainViewModel.testCurrentServerRealPing()
        } else {
//                tv_test_state.text = getString(R.string.connection_test_fail)
        }

    }

    fun fabClick() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
        } else if ((MmkvManager.decodeSettingsString(AppConfig.PREF_MODE) ?: VPN) == VPN) {
            val intent = VpnService.prepare(this)
            if (intent == null) {
                startV2Ray()
            } else {
                requestVpnPermission.launch(intent)
            }
        } else {
            startV2Ray()
        }
    }


    fun showDeleteConfigDialog(guid: String, profile: ProfileItem, position: Int) {
        deleteConfigFragment = DeleteConfigFragment {
            showUndoToast(guid, profile, position)
            configsFragment.adapter.removeServer(guid, position)
        }
        if (!deleteConfigFragment.isAdded) {
            deleteConfigFragment.show(supportFragmentManager, "deleteConfigFragment")
        }
    }

    fun showGameModeInfo() {
        gameModeInfoFragment = GameModeInfoFragment()
        if (!gameModeInfoFragment.isAdded) {
            gameModeInfoFragment.show(supportFragmentManager, "deleteConfigFragment")
        }
    }

    fun initFragments() {

        homeFragment = HomeFragment(this)
        configsFragment = ConfigsFragment(this)
        logsFragment = LogsFragment()
        settingFragment = SettingFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.frameLayout, homeFragment, "homeFragment")
            .add(R.id.frameLayout, configsFragment, "configsFragment")
            .add(R.id.frameLayout, logsFragment, "logsFragment")
            .add(R.id.frameLayout, settingFragment, "settingFragment")
            .hide(configsFragment)
            .hide(logsFragment)
            .hide(settingFragment)
            .commit()

    }

    fun bottomNavColorChange() {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.grays600)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = true
        }
    }


    override fun onBackPressed() {
        when {
            homeFragment.isVisible -> {
                // If the homeFragment is currently visible, proceed with the default back press behavior
                super.onBackPressed()
            }

            else -> {
                // If any other fragment is visible, navigate back to homeFragment
                setHomeFragment()
            }
        }
    }

    fun setupBottomNav() {


        binding.bottomNavCV.setOnClickListener {

        }

        binding.bottomNavHomeLl.setOnClickListener {
            setHomeFragment()
        }

        binding.bottomNavConfigsLl.setOnClickListener {
            setConfigsFragment()
        }

        binding.bottomNavLogsLl.setOnClickListener {
            setLogsFragment()
        }

        binding.bottomNavSettingLl.setOnClickListener {
            setSettingFragment()
        }

    }

    fun setHomeFragment() {
        deselectAll()
        binding.bottomNavHomeIv.setImageResource(R.drawable.ic_home_selected)
        binding.bottomNavHomeTv.setTextColor(resources.getColor(R.color.primary600))
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .hide(configsFragment)
            .hide(logsFragment)
            .hide(settingFragment)
            .show(homeFragment)
            .commit()

    }

    fun setConfigsFragment() {
        deselectAll()
        binding.bottomNavConfigsIv.setImageResource(R.drawable.ic_configs_selected)
        binding.bottomNavConfigsTv.setTextColor(resources.getColor(R.color.primary600))
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .hide(homeFragment)
            .hide(logsFragment)
            .hide(settingFragment)
            .show(configsFragment)
            .commit()
    }

    fun setLogsFragment() {
        logsFragment.onRefresh()
        deselectAll()
        binding.bottomNavLogsIv.setImageResource(R.drawable.ic_logs_selected)
        binding.bottomNavLogsTv.setTextColor(resources.getColor(R.color.primary600))
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .hide(homeFragment)
            .hide(configsFragment)
            .hide(settingFragment)
            .show(logsFragment)
            .commit()
    }

    fun setSettingFragment() {
        deselectAll()
        binding.bottomNavSettingIv.setImageResource(R.drawable.ic_setting_selected)
        binding.bottomNavSettingTv.setTextColor(resources.getColor(R.color.primary600))
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .hide(homeFragment)
            .hide(configsFragment)
            .hide(logsFragment)
            .show(settingFragment)
            .commit()
    }

    fun deselectAll() {
        binding.bottomNavHomeIv.setImageResource(R.drawable.ic_home_deselected)
        binding.bottomNavConfigsIv.setImageResource(R.drawable.ic_configs_deselected)
        binding.bottomNavLogsIv.setImageResource(R.drawable.ic_logs_deselected)
        binding.bottomNavSettingIv.setImageResource(R.drawable.ic_setting_deselected)
        binding.bottomNavHomeTv.setTextColor(resources.getColor(R.color.grays300))
        binding.bottomNavConfigsTv.setTextColor(resources.getColor(R.color.grays300))
        binding.bottomNavLogsTv.setTextColor(resources.getColor(R.color.grays300))
        binding.bottomNavSettingTv.setTextColor(resources.getColor(R.color.grays300))
    }

    fun showUndoToast(guid: String, profile: ProfileItem, position: Int) {
        val toastCard = binding.customToastSuccessfullyDeletedCV
        val undoButton = binding.undoButton


        toastCard.alpha = 0f
        toastCard.visibility = View.VISIBLE

        toastCard.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()

        val handler = Handler(Looper.getMainLooper())
        val removeToastRunnable = Runnable {
            toastCard.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { toastCard.visibility = View.GONE }
                .start()
        }


        handler.postDelayed(removeToastRunnable, 3000)


        undoButton.setOnClickListener {
            handler.removeCallbacks(removeToastRunnable)
            toastCard.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { toastCard.visibility = View.GONE }
                .start()
            // Add undo logic here (restore deleted config)
            mainViewModel.serversCache.add(position, ServersCache(guid, profile))
            MmkvManager.encodeServerConfig(guid, profile, position)
            Log.i("errrrrrrrrrrrrrrrr", "onBindViewHolder: ${guid + profile + position}")
            configsFragment.adapter.notifyItemInserted(position)
            toastCard.visibility = View.GONE
        }

    }

    fun showSuccessAddConfigToast() {
        val toastCard = binding.customToastSuccessfullyAddCV

        toastCard.alpha = 0f
        toastCard.visibility = View.VISIBLE

        toastCard.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()

        val handler = Handler(Looper.getMainLooper())
        val removeToastRunnable = Runnable {
            toastCard.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { toastCard.visibility = View.GONE }
                .start()
        }
        handler.postDelayed(removeToastRunnable, 3000)
        toastCard.visibility = View.GONE

    }

    fun showFailedToAddConfigToast() {
        val toastCard = binding.customToastFailedToAddCV

        toastCard.alpha = 0f
        toastCard.visibility = View.VISIBLE

        toastCard.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()

        val handler = Handler(Looper.getMainLooper())
        val removeToastRunnable = Runnable {
            toastCard.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { toastCard.visibility = View.GONE }
                .start()
        }
        handler.postDelayed(removeToastRunnable, 3000)
        toastCard.visibility = View.GONE

    }


    fun getFreeConfig() {

        configManager = ConfigManager(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null) {
                    viewModel.postLogin(ConfigsRequestBody(token, getDeviceModel())).observe(this)
                    {
                        it.body()?.let { configResponse ->
                            configManager.updateConfigs(configResponse)
                            val configList = configManager.getConfigList()
                        }
                    }
                }
            } else {
                Log.e("FCM", "Fetching FCM token failed", task.exception)
            }
        }


    }

    fun getDeviceModel(): String {
        val manufacturer = android.os.Build.MANUFACTURER.capitalize()
        val model: String? = android.os.Build.MODEL
        val brand: String? = android.os.Build.BRAND

        return if (model!!.startsWith(manufacturer)) {
            model
        } else {
            "${manufacturer!!} ${brand!!} ${model!!}"
        }
    }

    private fun setupViewModel() {
        mainViewModel.updateListAction.observe(this) { index ->
            if (index >= 0) {
                configsFragment.adapter.notifyItemChanged(index)
            } else {
                configsFragment.adapter.notifyDataSetChanged()
            }
        }
        mainViewModel.updateTestResultAction.observe(this) { homeFragment.setTestState(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            adapter.isRunning = isRunning
            if (isRunning) {
                binding.fab.setImageResource(R.drawable.ic_stop_24dp)
                binding.fab.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_active))
                homeFragment.setTestState(getString(R.string.connection_connected))
                binding.layoutTest.isFocusable = true
                homeFragment.turnVpnOn()
            } else {
                binding.fab.setImageResource(R.drawable.ic_play_24dp)
                binding.fab.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_inactive))
                homeFragment.setTestState(getString(R.string.connection_not_connected))
                binding.layoutTest.isFocusable = false
                homeFragment.turnVpnOff()
            }
        }
        mainViewModel.startListenBroadcast()
        mainViewModel.initAssets(assets)
    }

    private fun migrateLegacy() {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = MigrateManager.migrateServerConfig2Profile()
            launch(Dispatchers.Main) {
                if (result) {
                    toast(getString(R.string.migration_success))
                    mainViewModel.reloadServerList()
                } else {
                    //toast(getString(R.string.migration_fail))
                }
            }

        }
    }

    private fun initGroupTab() {
        binding.tabGroup.removeOnTabSelectedListener(tabGroupListener)
        binding.tabGroup.removeAllTabs()
        binding.tabGroup.isVisible = false

        val (listId, listRemarks) = mainViewModel.getSubscriptions(this)
        if (listId == null || listRemarks == null) {
            return
        }

        for (it in listRemarks.indices) {
            val tab = binding.tabGroup.newTab()
            tab.text = listRemarks[it]
            tab.tag = listId[it]
            binding.tabGroup.addTab(tab)
        }
        val selectIndex =
            listId.indexOf(mainViewModel.subscriptionId).takeIf { it >= 0 } ?: (listId.count() - 1)
        binding.tabGroup.selectTab(binding.tabGroup.getTabAt(selectIndex))
        binding.tabGroup.addOnTabSelectedListener(tabGroupListener)
        binding.tabGroup.isVisible = true
    }

    fun startV2Ray() {
        if (MmkvManager.getSelectServer().isNullOrEmpty()) {
            toast(R.string.title_file_chooser)
            return
        }
        V2RayServiceManager.startV2Ray(this)
    }

    fun restartV2Ray() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
        }
        Observable.timer(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startV2Ray()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkSpeedMonitor.stopMonitoring()
    }

    public override fun onResume() {
        super.onResume()
        mainViewModel.reloadServerList()
    }

    public override fun onPause() {
        super.onPause()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//
//        val searchItem = menu.findItem(R.id.search_view)
//        if (searchItem != null) {
//            val searchView = searchItem.actionView as SearchView
//            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String?): Boolean = false
//
//                override fun onQueryTextChange(newText: String?): Boolean {
//                    mainViewModel.filterConfig(newText.orEmpty())
//                    return false
//                }
//            })
//
//            searchView.setOnCloseListener {
//                mainViewModel.filterConfig("")
//                false
//            }
//        }
//        return super.onCreateOptionsMenu(menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.import_qrcode -> {
            importQRcode(true)
            true
        }

        R.id.import_clipboard -> {
            importClipboard()
            true
        }

        R.id.import_manually_vmess -> {
            importManually(EConfigType.VMESS.value)
            true
        }

        R.id.import_manually_vless -> {
            importManually(EConfigType.VLESS.value)
            true
        }

        R.id.import_manually_ss -> {
            importManually(EConfigType.SHADOWSOCKS.value)
            true
        }

        R.id.import_manually_socks -> {
            importManually(EConfigType.SOCKS.value)
            true
        }

        R.id.import_manually_http -> {
            importManually(EConfigType.HTTP.value)
            true
        }

        R.id.import_manually_trojan -> {
            importManually(EConfigType.TROJAN.value)
            true
        }

        R.id.import_manually_wireguard -> {
            importManually(EConfigType.WIREGUARD.value)
            true
        }

        R.id.import_manually_hysteria2 -> {
            importManually(EConfigType.HYSTERIA2.value)
            true
        }

        R.id.import_config_custom_clipboard -> {
            importConfigCustomClipboard()
            true
        }

        R.id.import_config_custom_local -> {
            importConfigCustomLocal()
            true
        }

        R.id.import_config_custom_url -> {
            importConfigCustomUrlClipboard()
            true
        }

        R.id.import_config_custom_url_scan -> {
            importQRcode(false)
            true
        }

        R.id.sub_update -> {
            importConfigViaSub()
            true
        }

        R.id.export_all -> {
            binding.pbWaiting.show()
            lifecycleScope.launch(Dispatchers.IO) {
                val ret = mainViewModel.exportAllServer()
                launch(Dispatchers.Main) {
                    if (ret > 0)
                        toast(getString(R.string.title_export_config_count, ret))
                    else
                        toast(R.string.toast_failure)
                    binding.pbWaiting.hide()
                }
            }

            true
        }

        R.id.ping_all -> {
            toast(
                getString(
                    R.string.connection_test_testing_count,
                    mainViewModel.serversCache.count()
                )
            )
            mainViewModel.testAllTcping()
            true
        }

        R.id.real_ping_all -> {
            toast(
                getString(
                    R.string.connection_test_testing_count,
                    mainViewModel.serversCache.count()
                )
            )
            mainViewModel.testAllRealPing()
            true
        }

        R.id.service_restart -> {
            restartV2Ray()
            true
        }

        R.id.del_all_config -> {
            AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    binding.pbWaiting.show()
                    lifecycleScope.launch(Dispatchers.IO) {
                        val ret = mainViewModel.removeAllServer()
                        launch(Dispatchers.Main) {
                            mainViewModel.reloadServerList()
                            toast(getString(R.string.title_del_config_count, ret))
                            binding.pbWaiting.hide()
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    //do noting
                }
                .show()
            true
        }

        R.id.del_duplicate_config -> {
            AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    binding.pbWaiting.show()
                    lifecycleScope.launch(Dispatchers.IO) {
                        val ret = mainViewModel.removeDuplicateServer()
                        launch(Dispatchers.Main) {
                            mainViewModel.reloadServerList()
                            toast(getString(R.string.title_del_duplicate_config_count, ret))
                            binding.pbWaiting.hide()
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    //do noting
                }
                .show()
            true
        }

        R.id.del_invalid_config -> {
            AlertDialog.Builder(this).setMessage(R.string.del_invalid_config_comfirm)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    binding.pbWaiting.show()
                    lifecycleScope.launch(Dispatchers.IO) {
                        val ret = mainViewModel.removeInvalidServer()
                        launch(Dispatchers.Main) {
                            mainViewModel.reloadServerList()
                            toast(getString(R.string.title_del_config_count, ret))
                            binding.pbWaiting.hide()
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    //do noting
                }
                .show()
            true
        }

        R.id.sort_by_test_results -> {
            binding.pbWaiting.show()
            lifecycleScope.launch(Dispatchers.IO) {
                mainViewModel.sortByTestResults()
                launch(Dispatchers.Main) {
                    mainViewModel.reloadServerList()
                    binding.pbWaiting.hide()
                }
            }
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun importManually(createConfigType: Int) {
        startActivity(
            Intent()
                .putExtra("createConfigType", createConfigType)
                .putExtra("subscriptionId", mainViewModel.subscriptionId)
                .setClass(this, ServerActivity::class.java)
        )
    }

    /**
     * import config from qrcode
     */
    fun importQRcode(forConfig: Boolean): Boolean {
//        try {
//            startActivityForResult(Intent("com.google.zxing.client.android.SCAN")
//                    .addCategory(Intent.CATEGORY_DEFAULT)
//                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), requestCode)
//        } catch (e: Exception) {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe {
                if (it)
                    if (forConfig)
                        scanQRCodeForConfig.launch(Intent(this, ScannerActivity::class.java))
                    else
                        scanQRCodeForUrlToCustomConfig.launch(
                            Intent(
                                this,
                                ScannerActivity::class.java
                            )
                        )
                else
                    toast(R.string.toast_permission_denied)
            }
//        }
        return true
    }

    private val scanQRCodeForConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                importBatchConfig(it.data?.getStringExtra("SCAN_RESULT"))
            }
        }

    private val scanQRCodeForUrlToCustomConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                importConfigCustomUrl(it.data?.getStringExtra("SCAN_RESULT"))
            }
        }

    /**
     * import config from clipboard
     */
    fun importClipboard()
            : Boolean {
        try {
            val clipboard = Utils.getClipboard(this)
            importBatchConfig(clipboard)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun importBatchConfig(server: String?) {
        binding.pbWaiting.show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val (count, countSub) = AngConfigManager.importBatchConfig(
                    server,
                    mainViewModel.subscriptionId,
                    true
                )
                delay(500L)
                withContext(Dispatchers.Main) {
                    when {
                        count > 0 -> {
                            toast(getString(R.string.title_import_config_count, count))
                            binding.textSuccessImportConfigTV.text =
                                count.toString() + " configs have been successfully added."
                            showSuccessAddConfigToast()
                            mainViewModel.reloadServerList()
                        }

                        countSub > 0 -> initGroupTab()
                        else -> {
                            showFailedToAddConfigToast()
                            //toast(R.string.toast_failure)
                        }
                    }
                    binding.pbWaiting.hide()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //toast(R.string.toast_failure)
                    showFailedToAddConfigToast()
                    binding.pbWaiting.hide()
                }
                e.printStackTrace()
            }
        }
    }


    private fun importConfigCustomClipboard()
            : Boolean {
        try {
            val configText = Utils.getClipboard(this)
            if (TextUtils.isEmpty(configText)) {
                toast(R.string.toast_none_data_clipboard)
                return false
            }
            importCustomizeConfig(configText)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * import config from local config file
     */
    fun importConfigCustomLocal(): Boolean {
        try {
            showFileChooser()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun importConfigCustomUrlClipboard()
            : Boolean {
        try {
            val url = Utils.getClipboard(this)
            if (TextUtils.isEmpty(url)) {
                toast(R.string.toast_none_data_clipboard)
                return false
            }
            return importConfigCustomUrl(url)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * import config from url
     */
    private fun importConfigCustomUrl(url: String?): Boolean {
        try {
            if (!Utils.isValidUrl(url)) {
                toast(R.string.toast_invalid_url)
                return false
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val configText = try {
                    Utils.getUrlContentWithCustomUserAgent(url)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
                launch(Dispatchers.Main) {
                    importCustomizeConfig(configText)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * import config from sub
     */
    private fun importConfigViaSub(): Boolean {
//        val dialog = AlertDialog.Builder(this)
//            .setView(LayoutProgressBinding.inflate(layoutInflater).root)
//            .setCancelable(false)
//            .show()
        binding.pbWaiting.show()

        lifecycleScope.launch(Dispatchers.IO) {
            val count = mainViewModel.updateConfigViaSubAll()
            delay(500L)
            launch(Dispatchers.Main) {
                if (count > 0) {
                    toast(getString(R.string.title_update_config_count, count))
                    mainViewModel.reloadServerList()
                } else {
                    toast(R.string.toast_failure)
                }
                //dialog.dismiss()
                binding.pbWaiting.hide()
            }
        }
        return true
    }

    /**
     * show file chooser
     */
    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            chooseFileForCustomConfig.launch(
                Intent.createChooser(
                    intent,
                    getString(R.string.title_file_chooser)
                )
            )
        } catch (ex: ActivityNotFoundException) {
            toast(R.string.toast_require_file_manager)
        }
    }

    private val chooseFileForCustomConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if (it.resultCode == RESULT_OK && uri != null) {
                readContentFromUri(uri)
            }
        }

    /**
     * read content from uri
     */
    private fun readContentFromUri(uri: Uri) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        RxPermissions(this)
            .request(permission)
            .subscribe {
                if (it) {
                    try {
                        contentResolver.openInputStream(uri).use { input ->
                            importCustomizeConfig(input?.bufferedReader()?.readText())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else
                    toast(R.string.toast_permission_denied)
            }
    }

    /**
     * import customize config
     */
    private fun importCustomizeConfig(server: String?) {
        try {
            if (server == null || TextUtils.isEmpty(server)) {
                toast(R.string.toast_none_data)
                return
            }
            if (mainViewModel.appendCustomConfigServer(server)) {
                mainViewModel.reloadServerList()
                toast(R.string.toast_success)
            } else {
                toast(R.string.toast_failure)
            }
            //adapter.notifyItemInserted(mainViewModel.serverList.lastIndex)
        } catch (e: Exception) {
            ToastCompat.makeText(
                this,
                "${getString(R.string.toast_malformed_josn)} ${e.cause?.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
            return
        }
    }

    private fun setTestState(content: String?) {
        binding.tvTestState.text = content
    }

//    val mConnection = object : ServiceConnection {
//        override fun onServiceDisconnected(name: ComponentName?) {
//        }
//
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            sendMsg(AppConfig.MSG_REGISTER_CLIENT, "")
//        }
//    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.sub_setting -> {
                requestSubSettingActivity.launch(Intent(this, SubSettingActivity::class.java))
            }

            R.id.settings -> {
                startActivity(
                    Intent(this, SettingsActivity::class.java)
                        .putExtra("isRunning", mainViewModel.isRunning.value == true)
                )
            }

            R.id.routing_setting -> {
                requestSubSettingActivity.launch(Intent(this, RoutingSettingActivity::class.java))
            }


            R.id.promotion -> {
                Utils.openUri(
                    this,
                    "${Utils.decode(AppConfig.PromotionUrl)}?t=${System.currentTimeMillis()}"
                )
            }

            R.id.logcat -> {
                startActivity(Intent(this, LogcatActivity::class.java))
            }

            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
