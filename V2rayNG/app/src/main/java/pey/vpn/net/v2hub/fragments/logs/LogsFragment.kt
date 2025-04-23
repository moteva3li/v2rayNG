package pey.vpn.net.fragments.logs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import pey.vpn.net.AppConfig.ANG_PACKAGE
import pey.vpn.net.R
import pey.vpn.net.databinding.FragmentLogsBinding
import pey.vpn.net.ui.LogcatRecyclerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class LogsFragment() : Fragment() , SwipeRefreshLayout.OnRefreshListener {

    lateinit var binding : FragmentLogsBinding

    var logsetsAll: MutableList<String> = mutableListOf()
    var logsets: MutableList<String> = mutableListOf()
    private val adapter by lazy { LogcatRecyclerAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLogsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupAdapter()

    }

    fun setupAdapter(){
        binding.logsRV.adapter = adapter
        binding.logsRV.layoutManager = LinearLayoutManager(requireActivity() , LinearLayoutManager.VERTICAL , false)
        binding.refreshLayout.setOnRefreshListener(this)
    }

    fun setupClickListeners(){

        binding.cleanLogsIV.setOnClickListener {
            clearLogcat()
        }

    }

    private fun getLogcat() {

        try {
            binding.refreshLayout.isRefreshing = true

            lifecycleScope.launch(Dispatchers.Default) {
                val lst = LinkedHashSet<String>()
                lst.add("logcat")
                lst.add("-d")
                lst.add("-v")
                lst.add("time")
                lst.add("-s")
                lst.add("GoLog,tun2socks,$ANG_PACKAGE,AndroidRuntime,System.err")
                val process = withContext(Dispatchers.IO) {
                    Runtime.getRuntime().exec(lst.toTypedArray())
                }

                val allText = process.inputStream.bufferedReader().use { it.readLines() }.reversed()

                Log.i("" +
                        ".", "getLogcat: ${allText}")
                launch(Dispatchers.Main) {
                    logsetsAll = allText.toMutableList()
                    logsets = allText.toMutableList()
                    adapter.notifyDataSetChanged()
                    binding.refreshLayout.isRefreshing = false
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun clearLogcat() {
        try {
            lifecycleScope.launch(Dispatchers.Default) {
                val lst = LinkedHashSet<String>()
                lst.add("logcat")
                lst.add("-c")
                withContext(Dispatchers.IO) {
                    val process = Runtime.getRuntime().exec(lst.toTypedArray())
                    process.waitFor()
                }
                launch(Dispatchers.Main) {
                    logsetsAll.clear()
                    logsets.clear()
                    adapter.notifyDataSetChanged()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onRefresh() {
        getLogcat()
    }

}