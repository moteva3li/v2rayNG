package pey.vpn.net.v2hub.adapter

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import pey.vpn.net.AngApplication.Companion.application
import pey.vpn.net.AppConfig
import pey.vpn.net.R
import pey.vpn.net.databinding.DialogShareOptionsBinding
import pey.vpn.net.databinding.ItemQrcodeBinding
import pey.vpn.net.databinding.ItemRecyclerFooterBinding
import pey.vpn.net.databinding.StylePersonalConfigBinding
import pey.vpn.net.dto.EConfigType
import pey.vpn.net.dto.ProfileItem
import pey.vpn.net.extension.toast
import pey.vpn.net.handler.AngConfigManager
import pey.vpn.net.handler.MmkvManager
import pey.vpn.net.helper.ItemTouchHelperAdapter
import pey.vpn.net.helper.ItemTouchHelperViewHolder
import pey.vpn.net.service.V2RayServiceManager
import pey.vpn.net.ui.MainActivity
import pey.vpn.net.ui.ServerActivity
import pey.vpn.net.ui.ServerCustomConfigActivity
import pey.vpn.net.util.Utils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class PersonalConfigAdapter(val activity: MainActivity) : RecyclerView.Adapter<PersonalConfigAdapter.BaseViewHolder>(),
    ItemTouchHelperAdapter {
    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_FOOTER = 2
    }

    private var mActivity: MainActivity = activity
    private val share_method: Array<out String> by lazy {
        mActivity.resources.getStringArray(R.array.share_method)
    }
    var isRunning = false

    override fun getItemCount() = mActivity.mainViewModel.serversCache.size + 1

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            val guid = mActivity.mainViewModel.serversCache[position].guid
            val profile = mActivity.mainViewModel.serversCache[position].profile
//            //filter
//            if (mActivity.mainViewModel.subscriptionId.isNotEmpty()
//                && mActivity.mainViewModel.subscriptionId != config.subscriptionId
//            ) {
//                holder.itemMainBinding.cardView.visibility = View.GONE
//            } else {
//                holder.itemMainBinding.cardView.visibility = View.VISIBLE
//            }

            val aff = MmkvManager.decodeServerAffiliationInfo(guid)

            holder.itemMainBinding.tvName.text = profile.remarks
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.itemMainBinding.tvTestResult.text = aff?.getTestDelayString().orEmpty()

            val testDelay = aff?.testDelayMillis ?: 0L
            holder.itemMainBinding.tvTestResult.setTextColor(
                when {
                    testDelay < 0L -> ContextCompat.getColor(mActivity, R.color.errorShade)
                    testDelay in 1L..580L -> ContextCompat.getColor(mActivity, R.color.successColor)
                    testDelay in 581L..790L -> ContextCompat.getColor(mActivity, R.color.warningColor)
                    else -> ContextCompat.getColor(mActivity, R.color.errorShade)
                }
            )

//            if ((aff?.testDelayMillis ?: 0L) < 0L) {
//                holder.itemMainBinding.tvTestResult.setTextColor(ContextCompat.getColor(mActivity, R.color.errorShade))
//            } else if((aff?.testDelayMillis ?: 0L) < 480L && (aff?.testDelayMillis ?: 0L) > 1L) {
//                holder.itemMainBinding.tvTestResult.setTextColor(ContextCompat.getColor(mActivity, R.color.successColor))
//            }else if ((aff?.testDelayMillis ?: 0L) < 750L && (aff?.testDelayMillis ?: 0L) > 481L){
//                holder.itemMainBinding.tvTestResult.setTextColor(ContextCompat.getColor(mActivity, R.color.warningColor))
//            }else{
//                holder.itemMainBinding.tvTestResult.setTextColor(ContextCompat.getColor(mActivity, R.color.errorShade))
//            }

            if (guid == MmkvManager.getSelectServer()) {
                holder.itemMainBinding.layoutIndicator.visibility = View.VISIBLE
            } else {
                holder.itemMainBinding.layoutIndicator.visibility = View.INVISIBLE
            }
            //holder.itemMainBinding.tvSubscription.text = MmkvManager.decodeSubscription(profile.subscriptionId)?.remarks ?: ""

            var shareOptions = share_method.asList()
            when (profile.configType) {
                EConfigType.CUSTOM -> {
                    holder.itemMainBinding.tvType.text = mActivity.getString(R.string.server_customize_config)
                    shareOptions = shareOptions.takeLast(1)
                }

                else -> {
                    holder.itemMainBinding.tvType.text = profile.configType.name
                }
            }

            // 隐藏主页服务器地址为xxx:xxx:***/xxx.xxx.xxx.***
            val strState = "${
                profile.server?.let {
                    if (it.contains(":"))
                        it.split(":").take(2).joinToString(":", postfix = ":***")
                    else
                        it.split('.').dropLast(1).joinToString(".", postfix = ".***")
                }
            } : ${profile.serverPort}"

            holder.itemMainBinding.tvStatistics.text = strState

            holder.itemMainBinding.layoutShare.setOnClickListener {
                showShareOptionsDialog(guid, profile)
            }

            holder.itemMainBinding.layoutEdit.setOnClickListener {
                val intent = Intent().putExtra("guid", guid)
                    .putExtra("isRunning", isRunning)
                    .putExtra("createConfigType", profile.configType.value)
                if (profile.configType == EConfigType.CUSTOM) {
                    mActivity.startActivity(intent.setClass(mActivity, ServerCustomConfigActivity::class.java))
                } else {
                    mActivity.startActivity(intent.setClass(mActivity, ServerActivity::class.java))
                }
            }

            holder.itemMainBinding.layoutRemove.setOnClickListener {
                if (guid != MmkvManager.getSelectServer()) {
                    if (MmkvManager.decodeSettingsBool(AppConfig.PREF_CONFIRM_REMOVE) == true) {
                        AlertDialog.Builder(mActivity).setMessage(R.string.del_config_comfirm)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                mActivity.showDeleteConfigDialog(guid, profile, position)
                                //removeServer(guid, position)
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ ->
                                //do noting
                            }
                            .show()
                    } else {
                        mActivity.showDeleteConfigDialog(guid, profile, position)
                        Log.i("errrrrrrrrrrrrrrrr", "onBindViewHolder: ${guid + profile + position}")
                        //removeServer(guid, position)
                    }
                } else {
                    application.toast(R.string.toast_action_not_allowed)
                }
            }

            holder.itemMainBinding.infoContainer.setOnClickListener {
                val selected = MmkvManager.getSelectServer()
                if (guid != selected) {
                    MmkvManager.setSelectServer(guid)
                    mActivity.homeFragment.setupPageDetails()
                    if (!TextUtils.isEmpty(selected)) {
                        notifyItemChanged(mActivity.mainViewModel.getPosition(selected.orEmpty()))
                    }
                    notifyItemChanged(mActivity.mainViewModel.getPosition(guid))
                    if (isRunning) {
                        Utils.stopVService(mActivity)
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                V2RayServiceManager.startV2Ray(mActivity)
                            }
                    }
                }
            }
        }
        if (holder is FooterViewHolder) {
            //if (activity?.defaultDPreference?.getPrefBoolean(AppConfig.PREF_INAPP_BUY_IS_PREMIUM, false)) {
            if (true) {
                holder.itemFooterBinding.layoutEdit.visibility = View.INVISIBLE
            } else {
                holder.itemFooterBinding.layoutEdit.setOnClickListener {
                    Utils.openUri(mActivity, "${Utils.decode(AppConfig.PromotionUrl)}?t=${System.currentTimeMillis()}")
                }
            }
        }
    }

    private fun showShareOptionsDialog(guid: String, profile: ProfileItem) {
        val dialog = Dialog(mActivity)
        val binding = DialogShareOptionsBinding.inflate(LayoutInflater.from(mActivity))
        dialog.setContentView(binding.root)

        // Make the dialog background rounded and transparent
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.tvShowQr.setOnClickListener {
            dialog.dismiss()
            if (profile.configType == EConfigType.CUSTOM) {
                shareFullContent(guid)
            } else {
                val ivBinding = ItemQrcodeBinding.inflate(LayoutInflater.from(mActivity))
                ivBinding.ivQcode.setImageBitmap(AngConfigManager.share2QRCode(guid))
                AlertDialog.Builder(mActivity).setView(ivBinding.root).show()
            }
        }

        binding.tvCopyUri.setOnClickListener {
            dialog.dismiss()
            if (AngConfigManager.share2Clipboard(mActivity, guid) == 0) {
                mActivity.toast(R.string.toast_success)
            } else {
                mActivity.toast(R.string.toast_failure)
            }
        }

        binding.tvCopyV2HubJson.setOnClickListener {
            dialog.dismiss()
            shareFullContent(guid)
        }

//        binding.tvLockAndShare.setOnClickListener {
//            dialog.dismiss()
//            mActivity.toast("else")
//        }

        dialog.show()
    }


    private fun shareFullContent(guid: String) {
        if (AngConfigManager.shareFullContent2Clipboard(mActivity, guid) == 0) {
            mActivity.toast(R.string.toast_success)
        } else {
            mActivity.toast(R.string.toast_failure)
        }
    }

    fun removeServer(guid: String, position: Int) {
        mActivity.mainViewModel.removeServer(guid)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mActivity.mainViewModel.serversCache.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM ->
                MainViewHolder(StylePersonalConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            else ->
                FooterViewHolder(ItemRecyclerFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mActivity.mainViewModel.serversCache.size) {
            VIEW_TYPE_FOOTER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    class MainViewHolder(val itemMainBinding: StylePersonalConfigBinding) :
        BaseViewHolder(itemMainBinding.root), ItemTouchHelperViewHolder

    class FooterViewHolder(val itemFooterBinding: ItemRecyclerFooterBinding) :
        BaseViewHolder(itemFooterBinding.root)

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        mActivity.mainViewModel.swapServer(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemMoveCompleted() {
        // do nothing
    }

    override fun onItemDismiss(position: Int) {
    }
}