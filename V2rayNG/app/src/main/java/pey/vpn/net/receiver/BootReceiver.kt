package pey.vpn.net.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pey.vpn.net.handler.MmkvManager
import pey.vpn.net.service.V2RayServiceManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //Check if context is not null and action is the one we want
        if (context == null || intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        //Check if flag is true and a server is selected
        if (!MmkvManager.decodeStartOnBoot() || MmkvManager.getSelectServer().isNullOrEmpty()) return
        //Start v2ray
        V2RayServiceManager.startV2Ray(context)
    }
}
