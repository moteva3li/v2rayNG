package com.v2ray.ang.fragments.configs

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.v2ray.ang.R
import com.v2ray.ang.databinding.FragmentConfigsBinding
import com.v2ray.ang.ui.MainActivity
import com.v2ray.ang.ui.MainRecyclerAdapter
import com.v2ray.ang.v2hub.adapter.PersonalConfigAdapter
import com.v2ray.ang.v2hub.sharedPrefrences.SharedPrefrences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ConfigsFragment(mainActivity: MainActivity) : Fragment() {

    lateinit var binding: FragmentConfigsBinding
    private lateinit var sortByPopup: PopupWindow
    private lateinit var addConfigPopup: PopupWindow
    var mActivity = mainActivity

    var isTestingConfigs = false

    val adapter by lazy { PersonalConfigAdapter((requireActivity() as MainActivity)) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfigsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOnClikListeners()
        setupSortByPopup()
        setupAddConfigPopup()
        setupPersonalConfigAdapter()

    }

    fun setupPersonalConfigAdapter() {
        //set adapter to recycler
        binding.configsRV.adapter = adapter
        binding.configsRV.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
    }

    fun setupOnClikListeners() {
        binding.sortingCV.setOnClickListener { showSortByPopup(it) }
        binding.addConfigIV.setOnClickListener { showAddConfigPopup(it) }
        binding.tabFreeCV.setOnClickListener { freeTabs() }
        binding.tabPersonalCV.setOnClickListener { personalTabs() }
        binding.checkPingsCV.setOnClickListener {
            chekingPingConfigs()
        }
    }

    fun chekingPingConfigs() {
        if (!isTestingConfigs) {
            isTestingConfigs = true
            mActivity.mainViewModel.testAllRealPing()
            binding.checkPingsCV.setCardBackgroundColor(resources.getColor(R.color.primary600))
            binding.checkPingsTV.setTextColor(resources.getColor(R.color.grays600))
            binding.checkPingsIconIV.setColorFilter(resources.getColor(R.color.grays600))
            binding.checkPingsInfoIV.visibility = View.GONE
            binding.checkPingsInfoPB.visibility = View.VISIBLE
            binding.root.postDelayed({
                isTestingConfigs = false
                binding.checkPingsCV.setCardBackgroundColor(resources.getColor(R.color.grays500))
                binding.checkPingsTV.setTextColor(resources.getColor(R.color.white))
                binding.checkPingsIconIV.setColorFilter(resources.getColor(R.color.white))
                binding.checkPingsInfoIV.visibility = View.VISIBLE
                binding.checkPingsInfoPB.visibility = View.GONE
            }, 3828)

        }
    }

    fun personalTabs() {
        binding.tabFreeCV.setCardBackgroundColor(resources.getColor(R.color.grays500))
        binding.tabPersonalCV.setCardBackgroundColor(resources.getColor(R.color.grays400))
        //set new adapter to recycler
    }

    fun freeTabs() {
        binding.tabFreeCV.setCardBackgroundColor(resources.getColor(R.color.grays400))
        binding.tabPersonalCV.setCardBackgroundColor(resources.getColor(R.color.grays500))
        //set new adapter to recycler
    }

    private fun setupSortByPopup() {
        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_sort_by, null)

        val parentCardView = binding.sortingCV

        parentCardView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        sortByPopup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setOnDismissListener {
                binding.arrowIcon.rotation = 0f
                binding.sortingCV.setCardBackgroundColor(resources.getColor(R.color.grays500))
            }
        }

        binding.sortedByTV.text = "Sort By ${SharedPrefrences().getSortedByFilter(requireContext())}"

        when(SharedPrefrences().getSortedByFilter(requireContext())){
            "Name" -> {
                popupView.findViewById<ImageView>(R.id.sortByNameIV).visibility = View.VISIBLE
            }
            "Time" -> {
                popupView.findViewById<ImageView>(R.id.sortByTimeIV).visibility = View.VISIBLE
            }
            "Ping" -> {
                popupView.findViewById<ImageView>(R.id.sortByPingIV).visibility = View.VISIBLE
            }
        }

        popupView.findViewById<LinearLayout>(R.id.sortByPingLL).setOnClickListener {
            popupView.findViewById<ImageView>(R.id.sortByNameIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByTimeIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByPingIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByPingIV).visibility = View.VISIBLE
            binding.sortedByTV.text = "Sort By Ping"
            lifecycleScope.launch(Dispatchers.IO) {
                mActivity.mainViewModel.sortByTestResults()
                launch(Dispatchers.Main) {
                    mActivity.mainViewModel.reloadServerList()
                }
            }
            SharedPrefrences().setSortedByFilter(requireContext(), "Ping")
            sortByPopup.dismiss()
        }

        popupView.findViewById<LinearLayout>(R.id.sortByTimeLL).setOnClickListener {
            popupView.findViewById<ImageView>(R.id.sortByNameIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByTimeIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByPingIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByTimeIV).visibility = View.VISIBLE
            binding.sortedByTV.text = "Sort By Time"
            lifecycleScope.launch(Dispatchers.IO) {
                mActivity.mainViewModel.sortByTime()
                launch(Dispatchers.Main) {
                    mActivity.mainViewModel.reloadServerList()
                }
            }
            SharedPrefrences().setSortedByFilter(requireContext(), "Time")
            sortByPopup.dismiss()
        }

        popupView.findViewById<LinearLayout>(R.id.sortByNameLL).setOnClickListener {
            popupView.findViewById<ImageView>(R.id.sortByNameIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByTimeIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByPingIV).visibility = View.GONE
            popupView.findViewById<ImageView>(R.id.sortByNameIV).visibility = View.VISIBLE
            binding.sortedByTV.text = "Sort By Name"
            lifecycleScope.launch(Dispatchers.IO) {
                mActivity.mainViewModel.sortByName()
                launch(Dispatchers.Main) {
                    mActivity.mainViewModel.reloadServerList()
                }
            }
            SharedPrefrences().setSortedByFilter(requireContext(), "Name")
            sortByPopup.dismiss()
        }

    }

    private fun setupAddConfigPopup() {
        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_add_config, null)

        addConfigPopup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setOnDismissListener {

            }
        }

        popupView.findViewById<TextView>(R.id.importConfig).setOnClickListener {
            addConfigPopup.dismiss()
        }

        popupView.findViewById<TextView>(R.id.importUrl).setOnClickListener {
            addConfigPopup.dismiss()
        }

        popupView.findViewById<TextView>(R.id.importJson).setOnClickListener {
            addConfigPopup.dismiss()
        }

        popupView.findViewById<TextView>(R.id.scanQr).setOnClickListener {
            addConfigPopup.dismiss()
        }

    }

    private fun showSortByPopup(anchorView: View) {
        binding.arrowIcon.rotation = 180f
        binding.sortingCV.setCardBackgroundColor(resources.getColor(R.color.grays600))
        val width = binding.sortingCV.width
        sortByPopup.width = width
        sortByPopup.showAsDropDown(anchorView)
    }

    private fun showAddConfigPopup(anchorView: View) {
        addConfigPopup.showAsDropDown(anchorView)
    }

}