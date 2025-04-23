package pey.vpn.net.ui

import android.os.Bundle
import pey.vpn.net.R
import pey.vpn.net.service.V2RayServiceManager
import pey.vpn.net.util.Utils

class ScSwitchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveTaskToBack(true)

        setContentView(R.layout.activity_none)

        if (V2RayServiceManager.v2rayPoint.isRunning) {
            Utils.stopVService(this)
        } else {
            Utils.startVServiceFromToggle(this)
        }
        finish()
    }
}
