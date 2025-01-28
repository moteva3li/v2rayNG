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


class HomeFragment : Fragment() {

    lateinit var binding : FragmentHomeBinding
    val mainViewModel: MainViewModel by viewModels()

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

        binding.vpnPowerCV.setOnClickListener {
            if (mainViewModel.isRunning.value == true) {
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

    fun turnVpnOn(){
        isON = true
        binding.lequidBackIV.visibility = View.VISIBLE
        binding.lequidFrontIV.visibility = View.VISIBLE
        binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.grays600))

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
        }, 2000)
    }

    fun turnVpnOff(){
        isON = false
        binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.grays600))
        binding.lequidBackIV.visibility = View.VISIBLE
        binding.lequidFrontIV.visibility = View.VISIBLE

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