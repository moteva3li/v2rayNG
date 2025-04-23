package pey.vpn.net.v2hub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import pey.vpn.net.R
import pey.vpn.net.api.Model.Config

class FreeConfigsAdapter(
    private val configList: List<Config>,
    private val onItemClick: (Config) -> Unit // Click listener
) : RecyclerView.Adapter<FreeConfigsAdapter.ConfigViewHolder>() {

    class ConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagIcon: RoundedImageView = itemView.findViewById(R.id.freeConfigFlagRIV)
        val countryName: TextView = itemView.findViewById(R.id.freeConfigNameTV)
        val ping: TextView = itemView.findViewById(R.id.freeConfigPingTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.style_free_config, parent, false)
        return ConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConfigViewHolder, position: Int) {
        val config = configList[position]

        // Set country name
        holder.countryName.text = config.country

        // Load flag image using Glide (or another image loading library)
        Glide.with(holder.itemView.context)
            .load(config.flag) // URL from your config
            .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder if image fails to load
            .into(holder.flagIcon)

        // For now, hardcoding the KCP and latency as in the image
        // You can modify this to fetch real data if available
        holder.ping.text = "28 ms"

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(config)
        }
    }

    override fun getItemCount(): Int = configList.size
}