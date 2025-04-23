package pey.vpn.net.fragments.setting

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import pey.vpn.net.v2hub.activities.AboutUsActivity
import pey.vpn.net.v2hub.activities.PrivacyPolicyActivity
import pey.vpn.net.databinding.FragmentSettingBinding
import pey.vpn.net.v2hub.sharedPrefrences.SharedPrefrences


class SettingFragment : Fragment() {

    lateinit var binding: FragmentSettingBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notificationSwitch.isChecked = SharedPrefrences().getNotificationStatus(requireContext())!!
        setupOnClickListeners()

    }

    fun setupOnClickListeners() {

        binding.supportCV.setOnClickListener {
            openGmail(
                context = requireContext(),
                emailAddress = "support@v2hub.com",
                subject = "Support Request",
                body = "Hello Support Team,\n\nI need help with..."
            )
        }

        binding.privacyPolicyCV.setOnClickListener {
            var intent = Intent(requireContext(), PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        binding.aboutUsCV.setOnClickListener {
            var intent = Intent(requireContext(), AboutUsActivity::class.java)
            startActivity(intent)
        }

        binding.notificationSwitch.setOnCheckedChangeListener { view, isChecked ->
            SharedPrefrences().setNotificationStatus(requireContext(), isChecked)
        }

    }

    fun openGmail(context: Context, emailAddress: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(
                Intent.createChooser(intent, "Choose an Email client")
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }


}