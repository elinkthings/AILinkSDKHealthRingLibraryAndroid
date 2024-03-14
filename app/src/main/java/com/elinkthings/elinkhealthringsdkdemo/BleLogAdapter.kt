package com.elinkthings.elinkhealthringsdkdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * @author suzy
 * @date 2024/3/13 17:18
 **/
class BleLogAdapter(
    private val list: List<String>,
) : RecyclerView.Adapter<BleLogAdapter.BleLogViewHolder>() {

    class BleLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLog: TextView = itemView.findViewById(R.id.tv_ble_log)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BleLogViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_ble_log_adapter, parent, false)
        return BleLogViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BleLogViewHolder, position: Int) {
        holder.tvLog.text = list[position]
    }

    override fun getItemCount(): Int = list.size
}