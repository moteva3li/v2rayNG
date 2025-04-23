package pey.vpn.net.v2hub.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import pey.vpn.net.R
import pey.vpn.net.databinding.FragmentDeleteConfigBinding


class DeleteConfigFragment(var onDeleteClicked: () -> Unit) : DialogFragment() {

    lateinit var binding : FragmentDeleteConfigBinding
    override fun getTheme() = R.style.NoBackgroundDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDeleteConfigBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deleteConfigDeletCV.setOnClickListener {
            onDeleteClicked.invoke()
            dismiss()
        }
        binding.deleteConfigCloseIV.setOnClickListener { dismiss() }
        binding.deleteConfigCancelLL.setOnClickListener { dismiss() }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.FullScreenDialogStyle)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

}