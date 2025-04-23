package pey.vpn.net.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.tbruyelle.rxpermissions3.RxPermissions
import pey.vpn.net.R
import pey.vpn.net.extension.toast
import pey.vpn.net.handler.AngConfigManager

class ScScannerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_none)
        importQRcode()
    }

    fun importQRcode(): Boolean {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
                    scanQRCode.launch(Intent(this, ScannerActivity::class.java))
                } else {
                    toast(R.string.toast_permission_denied)
                }
            }
        return true
    }


    private val scanQRCode = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val scanResult = it.data?.getStringExtra("SCAN_RESULT").orEmpty()
            val (count, countSub) = AngConfigManager.importBatchConfig(scanResult, "", false)

            if (count + countSub > 0) {
                toast(R.string.toast_success)
            } else {
                toast(R.string.toast_failure)
            }

            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

}
