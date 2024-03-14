package com.elinkthings.elinkhealthringsdkdemo

import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elinkthings.healthring.ElinkHealthRingBleData
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import com.pingwang.bluetoothlib.utils.BleLog

class ElinkMainActivity : ElinkBasePermissionActivity(), OnCallbackBle {

    private val bleDataList = mutableListOf<BleValueBean>()
    private val bleDataAdapter = BleDataAdapter(bleDataList) {
        ElinkHealthRingActivity.start(this@ElinkMainActivity, it.address, it.getCid(), it.getVid(), it.getPid())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elink_main)
        BleLog.init(true)
        with(findViewById<RecyclerView>(R.id.rv_main)) {
            layoutManager = LinearLayoutManager(this@ElinkMainActivity)
            adapter = bleDataAdapter
        }
        findViewById<Button>(R.id.btn_main_scan).setOnClickListener {
            startScan()
        }
        findViewById<Button>(R.id.btn_main_stop_scan).setOnClickListener {
            stopScan()
        }
    }

    override fun onPermissionOk() {

    }

    override fun onBindServiceSuccess() {
        aiLinkBleManager?.setOnCallbackBle(this)
        startScan()
    }

    override fun onBindServiceFailed() {
        stopScan()
    }

    override fun onScanning(data: BleValueBean) {
        if (data.getCid() != ElinkHealthRingBleData.ELINK_HEALTH_RING_CID) return //Filtering device that is not a ring
        val filterIndex = bleDataList.indexOfFirst { it.address.equals(data.address) }
        if (filterIndex != -1) {
            bleDataList[filterIndex].rssi = data.rssi
            bleDataAdapter.notifyItemChanged(filterIndex)
        } else {
            bleDataList.add(data)
            bleDataAdapter.notifyItemInserted(bleDataList.size -1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        aiLinkBleManager?.setOnCallbackBle(null)
        stopScan()
    }
}