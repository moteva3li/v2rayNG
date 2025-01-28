package com.v2ray.ang.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.databinding.ItemRecyclerLogcatBinding
import com.v2ray.ang.fragments.logs.LogsFragment

class LogcatRecyclerAdapter(var logsFragment: LogsFragment) : RecyclerView.Adapter<LogcatRecyclerAdapter.MainViewHolder>() {

    override fun getItemCount() = logsFragment.logsets.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val content = logsFragment.logsets[position]

        // Split the log entry into timestamp and message
        val logParts = content.split(" ", limit = 10)
        val timestamp = logParts.getOrNull(1) ?: ""
        val message = logParts.getOrNull(6) ?: content

        // Set the timestamp and message in the ViewHolder
        holder.itemSubSettingBinding.logTimeTv.text = timestamp
        holder.itemSubSettingBinding.logContentTv.text = message

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            ItemRecyclerLogcatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    class MainViewHolder(val itemSubSettingBinding: ItemRecyclerLogcatBinding) : RecyclerView.ViewHolder(itemSubSettingBinding.root)

}
