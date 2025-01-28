package com.v2ray.ang.fragments.home

import android.animation.ValueAnimator
import android.net.VpnService
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.v2ray.ang.AppConfig
import com.v2ray.ang.AppConfig.VPN
import com.v2ray.ang.R
import com.v2ray.ang.databinding.FragmentHomeBinding
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.ui.MainActivity
import com.v2ray.ang.util.Utils
import com.v2ray.ang.viewmodel.MainViewModel


class HomeFragment(mainActivity: MainActivity) : Fragment() {

    lateinit var binding : FragmentHomeBinding
    var mActivity = mainActivity
    var isON = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onClickListeners()
        setupPageDetails()

    }

    fun setupPageDetails(){
        val config = MmkvManager.decodeServerConfig(MmkvManager.getSelectServer()!!)
        if(config != null){
            binding.selectedConfigEmptyLL.visibility = View.GONE
            binding.selectedConfigFillLL.visibility = View.VISIBLE
            binding.selectedConfigNameTV.text = config.remarks
            binding.ipConfigSelectedTV.text = "IP " + "${
                config.server?.let {
                    if (it.contains(":"))
                        it.split(":").take(2).joinToString(":", postfix = ":***")
                    else
                        it.split('.').dropLast(1).joinToString(".", postfix = ".***")
                }
            } : ${config.serverPort}"

        }else{
            binding.selectedConfigEmptyLL.visibility = View.VISIBLE
            binding.selectedConfigFillLL.visibility = View.GONE
        }

    }

    fun onClickListeners(){

        binding.selectedConfigEmptyLL.setOnClickListener {
            mActivity.setConfigsFragment()
        }

        binding.tapToPingTV.setOnClickListener {
            if (mActivity.mainViewModel.isRunning.value == true) {
                setTestState(getString(R.string.connection_test_testing))
                mActivity.mainViewModel.testCurrentServerRealPing()
            } else {
//                tv_test_state.text = getString(R.string.connection_test_fail)
            }
        }

        binding.vpnPowerCV.setOnClickListener {
            if (mActivity.mainViewModel.isRunning.value == true) {
                Utils.stopVService(requireActivity())
            } else if ((MmkvManager.decodeSettingsString(AppConfig.PREF_MODE) ?: VPN) == VPN) {
                val intent = VpnService.prepare(requireActivity())
                if (intent == null) {
                    (requireActivity() as MainActivity).startV2Ray()
                } else {
                    (requireActivity() as MainActivity).requestVpnPermission.launch(intent)
                }
            } else {
                (requireActivity() as MainActivity).startV2Ray()
            }
            if (isON){
                turnVpnOff()
            }else{
                turnVpnOn()
            }

        }

    }

    fun setTestState(content: String?) {
        binding.delayTV.text = content
    }

    fun turnVpnOn(){
        isON = true
        binding.lequidBackIV.visibility = View.VISIBLE
        binding.lequidFrontIV.visibility = View.VISIBLE
        binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.grays600))

        binding.titleStatusConnectionTV.text = "Connecting"

        binding.lequidFrontIV.postDelayed({
            resizeImageView(binding.lequidFrontIV, binding.lequidFrontIV.width + 728, binding.lequidFrontIV.height + 728, 2000)
        }, 0)

        binding.lequidBackIV.postDelayed({
            resizeImageView(binding.lequidBackIV, binding.lequidBackIV.width + 728, binding.lequidBackIV.height + 728, 2000)
        }, 0)

        binding.vpnPowerCV.postDelayed({
            binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.primary600))
            binding.lequidBackIV.visibility = View.GONE
            binding.lequidFrontIV.visibility = View.GONE
            binding.titleStatusConnectionTV.text = "Connected"
            binding.subtitleStatusConnectionTV.text = "Fast & Secure"
        }, 2000)
    }

    fun turnVpnOff(){
        isON = false
        binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.grays600))
        binding.lequidBackIV.visibility = View.VISIBLE
        binding.lequidFrontIV.visibility = View.VISIBLE

        binding.titleStatusConnectionTV.text = "Disconnect"
        binding.subtitleStatusConnectionTV.text = "Select a server & tap to start"

        binding.lequidFrontIV.postDelayed({
            resizeImageView(binding.lequidFrontIV, binding.lequidFrontIV.width - 728, binding.lequidFrontIV.height - 728, 1500)
        }, 0)

        binding.lequidBackIV.postDelayed({
            resizeImageView(binding.lequidBackIV, binding.lequidBackIV.width - 728, binding.lequidBackIV.height - 728, 1500)
        }, 0)

        binding.vpnPowerCV.postDelayed({
            binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.grays100))
            binding.lequidBackIV.visibility = View.GONE
            binding.lequidFrontIV.visibility = View.GONE
        }, 1500)
    }

    private fun resizeImageView(imageView: ImageView, newWidth: Int, newHeight: Int, duration: Long = 1000) {
        val startWidth = imageView.width
        val startHeight = imageView.height

        val widthAnimator = ValueAnimator.ofInt(startWidth, newWidth)
        val heightAnimator = ValueAnimator.ofInt(startHeight, newHeight)

        widthAnimator.addUpdateListener { valueAnimator ->
            val layoutParams = imageView.layoutParams
            layoutParams.width = valueAnimator.animatedValue as Int
            imageView.layoutParams = layoutParams
        }

        heightAnimator.addUpdateListener { valueAnimator ->
            val layoutParams = imageView.layoutParams
            layoutParams.height = valueAnimator.animatedValue as Int
            imageView.layoutParams = layoutParams
        }

        widthAnimator.duration = duration
        heightAnimator.duration = duration

        widthAnimator.start()
        heightAnimator.start()
    }

}