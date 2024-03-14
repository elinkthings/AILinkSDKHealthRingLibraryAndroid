package com.elinkthings.elinkhealthringsdkdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.elinkthings.healthring.utils.toHex
import com.pingwang.bluetoothlib.bean.BleValueBean

/**
 * @author suzy
 * @date 2024/3/12 14:33
 **/
class BleDataAdapter(
    private val list: List<BleValueBean>,
    private val onItemClick: (BleValueBean) -> Unit
) : RecyclerView.Adapter<BleDataAdapter.BleDataViewHolder>() {

    class BleDataViewHolder(itemView: View) : ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_ble_name)
        val tvMac: TextView = itemView.findViewById(R.id.tv_ble_mac)
        val tvRssi: TextView = itemView.findViewById(R.id.tv_ble_rssi)
        val tvBleInfo: TextView = itemView.findViewById(R.id.tv_ble_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleDataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ble_data_adapter, parent, false)
        return BleDataViewHolder(itemView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: BleDataViewHolder, position: Int) {
        holder.tvName.text = list[position].name
        holder.tvMac.text = list[position].mac
        holder.tvRssi.text = list[position].rssi.toString()
        val cid = list[position].getCid()
        val vid = list[position].getVid()
        val pid = list[position].getPid()
        holder.tvBleInfo.text = "CID: ${cid.toHex()}($cid), VID: ${vid.toHex()}($vid), PID: ${pid.toHex()}($pid)"
        holder.itemView.setOnClickListener {
            onItemClick(list[position])
        }
    }
}