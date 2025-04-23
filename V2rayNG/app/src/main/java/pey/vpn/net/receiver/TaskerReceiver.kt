package pey.vpn.net.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import pey.vpn.net.AppConfig
import pey.vpn.net.handler.MmkvManager
import pey.vpn.net.service.V2RayServiceManager
import pey.vpn.net.util.Utils

class TaskerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        try {
            val bundle = intent?.getBundleExtra(AppConfig.TASKER_EXTRA_BUNDLE)
            val switch = bundle?.getBoolean(AppConfig.TASKER_EXTRA_BUNDLE_SWITCH, false)
            val guid = bundle?.getString(AppConfig.TASKER_EXTRA_BUNDLE_GUID).orEmpty()

            if (switch == null || TextUtils.isEmpty(guid)) {
                return
            } else if (switch) {
                if (guid == AppConfig.TASKER_DEFAULT_GUID) {
                    Utils.startVServiceFromToggle(context)
                } else {
                    MmkvManager.setSelectServer(guid)
                    V2RayServiceManager.startV2Ray(context)
                }
            } else {
                Utils.stopVService(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
