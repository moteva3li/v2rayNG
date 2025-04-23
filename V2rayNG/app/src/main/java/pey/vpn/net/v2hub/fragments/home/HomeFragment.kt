package pey.vpn.net.fragments.home

import android.animation.ValueAnimator
import android.net.VpnService
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.text.isDigitsOnly
import pey.vpn.net.AppConfig
import pey.vpn.net.AppConfig.VPN
import pey.vpn.net.R
import pey.vpn.net.databinding.FragmentHomeBinding
import pey.vpn.net.handler.MmkvManager
import pey.vpn.net.ui.MainActivity
import pey.vpn.net.util.Utils
import pey.vpn.net.v2hub.sharedPrefrences.SharedPrefrences


class HomeFragment(mainActivity: MainActivity) : Fragment() {

    lateinit var binding: FragmentHomeBinding
    var mActivity = mainActivity
    var firstInitForOffAnim = true

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

    fun setupPageDetails() {

        binding.gameModeSwitch.isChecked = SharedPrefrences().getSpeedStatus(requireContext())!!
        try {
            val config = MmkvManager.decodeServerConfig(MmkvManager.getSelectServer()!!)
            if (config != null) {
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
            } else {
                binding.selectedConfigEmptyLL.visibility = View.VISIBLE
                binding.selectedConfigFillLL.visibility = View.GONE
            }
        } catch (e: Exception) {

        }


    }

    fun onClickListeners() {

        binding.gameModeInfoIV.setOnClickListener { mActivity.showGameModeInfo() }
        binding.selectedConfigEmptyLL.setOnClickListener { mActivity.setConfigsFragment() }

        binding.tapToPingTV.setOnClickListener { mActivity.testPing() }

        binding.vpnPowerCV.setOnClickListener { mActivity.fabClick() }

        binding.gameModeSwitch.setOnCheckedChangeListener { view, isChecked ->
            SharedPrefrences().setSpeedStatus(requireContext(), isChecked)
        }


    }

    fun setTestState(content: String?) {
        try {
            if (SharedPrefrences().getSpeedStatus(requireContext())!!) {
                if (content!!.toLong() > 130L) {
                    binding.delayTV.text = (content.toLong() - 100L).toString()
                } else {
                    binding.delayTV.text = content
                }
            } else {
                binding.delayTV.text = content
            }
        }catch (e : Exception){

        }

    }

    fun turnVpnOn() {
        mActivity.networkSpeedMonitor.startMonitoring(1000, {
            binding.homeDownloadSpeedTV.text = it.downloadSpeed.toString() + " Mbps"
            binding.homeUploadSpeedTV.text = it.uploadSpeed.toString() + " Mbps"
        })

        mActivity.vpnTimer.resetTimer()
        mActivity.vpnTimer.startTimer { timeFormat ->
            binding.homeConnectedTimerTV.text =
                "${timeFormat.hours}:${timeFormat.minutes}:${timeFormat.seconds}"
        }

        binding.homeConnectionStatusTV.text = "Conected"

        binding.lequidBackIV.visibility = View.VISIBLE
        binding.lequidFrontIV.visibility = View.VISIBLE
        binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.grays600))

        binding.titleStatusConnectionTV.text = "Connecting"

        binding.lequidFrontIV.postDelayed({
            resizeImageView(
                binding.lequidFrontIV,
                binding.lequidFrontIV.width + 728,
                binding.lequidFrontIV.height + 728,
                2000
            )
        }, 0)

        binding.lequidBackIV.postDelayed({
            resizeImageView(
                binding.lequidBackIV,
                binding.lequidBackIV.width + 728,
                binding.lequidBackIV.height + 728,
                2000
            )
        }, 0)

        binding.vpnPowerCV.postDelayed({
            binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.primary600))
            binding.lequidBackIV.visibility = View.GONE
            binding.lequidFrontIV.visibility = View.GONE
            binding.titleStatusConnectionTV.text = "Connected"
            binding.subtitleStatusConnectionTV.text = "Fast & Secure"
        }, 2000)

    }

    fun turnVpnOff() {
        if (firstInitForOffAnim) {
            firstInitForOffAnim = false
            return
        }
        mActivity.networkSpeedMonitor.stopMonitoring()
        mActivity.vpnTimer.stopTimer()
        binding.homeConnectionStatusTV.text = "Disconected"

        binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.grays600))
        binding.lequidBackIV.visibility = View.VISIBLE
        binding.lequidFrontIV.visibility = View.VISIBLE

        binding.titleStatusConnectionTV.text = "Disconnect"
        binding.subtitleStatusConnectionTV.text = "Select a server & tap to start"

        binding.lequidFrontIV.postDelayed({
            resizeImageView(
                binding.lequidFrontIV,
                binding.lequidFrontIV.width - 728,
                binding.lequidFrontIV.height - 728,
                1500
            )
        }, 0)

        binding.lequidBackIV.postDelayed({
            resizeImageView(
                binding.lequidBackIV,
                binding.lequidBackIV.width - 728,
                binding.lequidBackIV.height - 728,
                1500
            )
        }, 0)

        binding.vpnPowerCV.postDelayed({
            binding.vpnPowerCV.setCardBackgroundColor(resources.getColor(R.color.grays100))
            binding.lequidBackIV.visibility = View.GONE
            binding.lequidFrontIV.visibility = View.GONE
        }, 1500)
    }

    private fun resizeImageView(
        imageView: ImageView,
        newWidth: Int,
        newHeight: Int,
        duration: Long = 1000
    ) {
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

    override fun onResume() {
        super.onResume()
        setupPageDetails()
    }

}