package pey.vpn.net.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import pey.vpn.net.R
import pey.vpn.net.databinding.ActivitySubSettingBinding
import pey.vpn.net.databinding.LayoutProgressBinding
import pey.vpn.net.dto.SubscriptionItem
import pey.vpn.net.extension.toast
import pey.vpn.net.handler.AngConfigManager
import pey.vpn.net.handler.MmkvManager
import pey.vpn.net.helper.SimpleItemTouchHelperCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SubSettingActivity : BaseActivity() {
    private val binding by lazy { ActivitySubSettingBinding.inflate(layoutInflater) }

    var subscriptions: List<Pair<String, SubscriptionItem>> = listOf()
    private val adapter by lazy { SubSettingRecyclerAdapter(this) }
    private var mItemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        title = getString(R.string.title_sub_setting)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        mItemTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(adapter))
        mItemTouchHelper?.attachToRecyclerView(binding.recyclerView)
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_sub_setting, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.add_config -> {
            startActivity(Intent(this, SubEditActivity::class.java))
            true
        }

        R.id.sub_update -> {
            val dialog = AlertDialog.Builder(this)
                .setView(LayoutProgressBinding.inflate(layoutInflater).root)
                .setCancelable(false)
                .show()

            lifecycleScope.launch(Dispatchers.IO) {
                val count = AngConfigManager.updateConfigViaSubAll()
                delay(500L)
                launch(Dispatchers.Main) {
                    if (count > 0) {
                        toast(R.string.toast_success)
                    } else {
                        toast(R.string.toast_failure)
                    }
                    dialog.dismiss()
                }
            }

            true
        }

        else -> super.onOptionsItemSelected(item)

    }

    fun refreshData() {
        subscriptions = MmkvManager.decodeSubscriptions()
        adapter.notifyDataSetChanged()
    }
}
