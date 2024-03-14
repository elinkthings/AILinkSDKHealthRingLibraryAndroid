package com.elinkthings.elinkhealthringsdkdemo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.annotation.IdRes
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elinkthings.bleotalibrary.listener.OnBleOTAListener
import com.elinkthings.healthring.ElinkHealthRingBleData
import com.elinkthings.healthring.bean.ElinkCheckupRealtimeData
import com.elinkthings.healthring.bean.ElinkRingDeviceStatus
import com.elinkthings.healthring.bean.ElinkRingHistoryData
import com.elinkthings.healthring.config.ElinkCheckupType
import com.elinkthings.healthring.config.ElinkSensorOTAErrorType
import com.elinkthings.healthring.impl.ImplHealthRingResult
import com.elinkthings.healthring.impl.ImplSensorOTA
import com.elinkthings.healthring.utils.toHexString
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.device.BleDevice
import com.pingwang.bluetoothlib.listener.OnBleVersionListener
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author suzy
 * @date 2024/3/13 16:54
 **/
class ElinkHealthRingActivity : ElinkBasePermissionActivity(), OnCallbackBle, OnBleVersionListener,
    ImplHealthRingResult {

    private val logList = mutableListOf<String>()
    private val logAdapter = BleLogAdapter(logList)
    private var rvBleLog: RecyclerView? = null
    private var ringBleData: ElinkHealthRingBleData? = null

    companion object {
        private const val EXTRA_MAC = "EXTRA_MAC"
        private const val EXTRA_CID = "EXTRA_CID"
        private const val EXTRA_VID = "EXTRA_VID"
        private const val EXTRA_PID = "EXTRA_PID"

        private val SENSOR_OTA_FILES =
            arrayOf("Sensor-202312271422-0x0407.bin", "Sensor-202401021530-0x0408.bin")
        private val BLE_OTA_FILES =
            arrayOf("BR01H1S1.0.0_20230923.img", "BR01H1S1.0.0_20240125.img")

        fun start(context: Context, mac: String, cid: Int, vid: Int, pid: Int) =
            Intent(context, ElinkHealthRingActivity::class.java).apply {
                putExtra(EXTRA_MAC, mac)
                putExtra(EXTRA_CID, cid)
                putExtra(EXTRA_VID, vid)
                putExtra(EXTRA_PID, pid)
                context.startActivity(this)
            }
    }

    private val mac: String
        get() = intent.getStringExtra(EXTRA_MAC) ?: ""

    private val bleData: BleValueBean?
        get() {
            val cid = intent.getIntExtra(EXTRA_CID, 0)
            val vid = intent.getIntExtra(EXTRA_VID, 0)
            val pid = intent.getIntExtra(EXTRA_PID, 0)
            if (mac.isEmpty()) {
                return null
            }
            return BleValueBean(mac, cid, vid, pid)
        }

    private val bleDevice: BleDevice?
        get() {
            return aiLinkBleManager?.getBleDevice(mac)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_elink_health_ring)
        initButton(R.id.btn_health_ring_connect) {
            connect()
        }
        initButton(R.id.btn_health_ring_disconnect) {
            aiLinkBleManager?.disconnect(mac)
        }
        initButton(R.id.btn_health_ring_device_state) {
            ringBleData?.getDeviceState()
        }
        initButton(R.id.btn_health_ring_checkup_duration) {
            ringBleData?.getCheckupDuration()
        }
        initButton(R.id.btn_health_ring_sensor_version) {
            ringBleData?.getSensorVersion()
        }
        initButton(R.id.btn_health_ring_checkup_type) {
            ringBleData?.getCheckupType()
        }
        initButton(R.id.btn_health_ring_auto_checkup) {
            ringBleData?.getAutoCheckState()
        }
        initButton(R.id.btn_health_ring_open_auto_checkup) {
            ringBleData?.openAutoCheck()
        }
        initButton(R.id.btn_health_ring_close_auto_checkup) {
            ringBleData?.closeAutoCheck()
        }
        initButton(R.id.btn_health_ring_close_auto_checkup) {
            ringBleData?.closeAutoCheck()
        }
        initButton(R.id.btn_health_ring_set_checkup_duration) {
            showListDialog(getString(R.string.set_checkup_duration), arrayOf("15", "30", "45", "60")) { item, _ ->
                ringBleData?.setCheckupDuration(item.toInt())
            }
        }
        initButton(R.id.btn_health_ring_set_checkup_type) {
            showListDialog(getString(R.string.set_checkup_type), arrayOf(getString(R.string.checkup_type_complex), getString(R.string.checkup_type_fast))) { _, position ->
                ringBleData?.setCheckupType(if (position == 0) ElinkCheckupType.COMPLEX else ElinkCheckupType.FAST)
            }
        }
        initButton(R.id.btn_health_ring_sensor_ota) {
            showListDialog(getString(R.string.select_sensor_ota_file), SENSOR_OTA_FILES) { item, _ ->
                lifecycleScope.launch {
                    val fileData = readBytesFromAssets(item)
                    ringBleData?.startSensorOTA(fileData)
                }
            }
        }
        initButton(R.id.btn_health_ring_ble_ota) {
            showListDialog(getString(R.string.select_ble_ota_file), BLE_OTA_FILES) { item, _ ->
                lifecycleScope.launch {
                    val file = copyAssetToCache(item)
                    file?.let {
                        ringBleData?.startBleOTA(it.path, object : OnBleOTAListener {
                            override fun onOtaSuccess() {
                                addLog("${getString(R.string.ble_ota)} onOtaSuccess")
                            }

                            override fun onOtaFailure(cmd: Int, err: String?) {
                                addLog("${getString(R.string.ble_ota)} onOtaFailure: $cmd, $err")
                            }

                            override fun onOtaProgress(
                                progress: Float,
                                currentCount: Int,
                                maxCount: Int,
                            ) {
                                addLog("${getString(R.string.ble_ota)} onOtaProgress: ${progress.toInt()} %, $currentCount, $maxCount")
                            }

                            override fun onOtaStatus(status: Int) {
                                addLog("${getString(R.string.ble_ota)} onOtaStatus: $status")
                            }

                            override fun onReconnect(mac: String?) {
                                addLog("${getString(R.string.ble_ota)} onReconnect: $mac")
                            }
                        })
                    }
                }
            }
        }
        initButton(R.id.btn_health_ring_get_history) {
            ringBleData?.getHistory()
        }
        initButton(R.id.btn_health_ring_get_next_history) {
            ringBleData?.getNextHistory()
        }
        initButton(R.id.btn_health_ring_get_history_over) {
            ringBleData?.getHistoryOver()
        }
        initButton(R.id.btn_health_ring_delete_history) {
            ringBleData?.deleteHistory()
        }
        initButton(R.id.btn_health_ring_start_checkup) {
            ringBleData?.startCheckup()
        }
        initButton(R.id.btn_health_ring_stop_checkup) {
            ringBleData?.stopCheckup()
        }
        initButton(R.id.btn_health_ring_sync_unix_time) {
            ringBleData?.syncUnixTime()
        }
        initButton(R.id.btn_health_ring_clear_log) {
            logList.clear()
            logAdapter.notifyDataSetChanged()
        }
        rvBleLog = findViewById<RecyclerView>(R.id.rv_ble_log).apply {
            layoutManager = LinearLayoutManager(this@ElinkHealthRingActivity)
            adapter = logAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initButton(@IdRes id: Int, onClick: () -> Unit) {
        findViewById<Button>(id).setOnClickListener {
            onClick()
        }
    }

    private fun connect() {
        bleData?.let {
            aiLinkBleManager?.connectDevice(it)
        }
    }

    override fun onPermissionOk() {

    }

    override fun onBindServiceSuccess() {
        aiLinkBleManager?.setOnCallbackBle(this)
        connect()
    }

    override fun onBindServiceFailed() {
        aiLinkBleManager?.setOnCallbackBle(null)
    }

    override fun onBmVersion(version: String) {
        addLog("${getString(R.string.ble_firmware_version)}: $version")
    }

    override fun bleOpen() {
        addLog(getString(R.string.ble_open))
    }

    override fun bleClose() {
        aiLinkBleManager?.disconnectAll()
        addLog(getString(R.string.ble_close))
    }

    override fun onConnecting(mac: String?) {
        addLog("${getString(R.string.connecting)}(${mac})")
    }

    override fun onConnectionSuccess(mac: String?) {
        addLog("${getString(R.string.connect_success)}(${mac})")
    }

    override fun onDisConnected(mac: String?, code: Int) {
        addLog("${getString(R.string.disconnect)}(${mac}, ${code})")
    }

    override fun onServicesDiscovered(mac: String?) {
        addLog("${getString(R.string.services_discovered)}(${mac})")
        bleDevice?.let {
            it.setOnBleVersionListener(this)
            ringBleData = ElinkHealthRingBleData(it)
            ringBleData?.setImplHealthRingResult(this)
            ringBleData?.setImplSensorOTA(object : ImplSensorOTA {
                override fun onFailure(type: ElinkSensorOTAErrorType) {
                    addLog("${getString(R.string.sensor_ota)} onFailure: $type")
                }

                override fun onSuccess() {
                    addLog("${getString(R.string.sensor_ota)} onSuccess")
                    ringBleData?.endSensorOTA()
                }

                override fun onProgress(progress: Int) {
                    addLog("${getString(R.string.sensor_ota)} onProgress: ${progress}%")
                }
            })
        }
    }

    private fun addLog(log: String) {
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault())
        logList.add("${dateFormat.format(Date(time))}: $log")
        logAdapter.notifyItemInserted(logList.size - 1)
        rvBleLog?.smoothScrollToPosition(logAdapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        ringBleData?.setImplHealthRingResult(null)
        ringBleData?.setImplSensorOTA(null)
        aiLinkBleManager?.setOnCallbackBle(null)
        bleDevice?.setOnBleVersionListener(null)
        aiLinkBleManager?.disconnect(mac)
    }

    override fun startCheckup(success: Boolean) {
        addLog("${getString(R.string.start_checkup)}: $success")
    }

    override fun stopCheckup(success: Boolean) {
        addLog("${getString(R.string.stop_checkup)}: $success")
    }

    override fun onGetRealtimeData(data: ElinkCheckupRealtimeData) {
        addLog("${getString(R.string.checkup_realtime_data)}: $data")
    }

    override fun onGetCheckupPackets(data: ByteArray) {
        addLog("${getString(R.string.checkup_packets)}: ${data.toHexString()}")
    }

    override fun onGetCheckupDuration(duration: Int) {
        addLog("${getString(R.string.checkup_duration)}: ${duration}${getString(R.string.time_minutes)}")
    }

    override fun onGetHistory(histories: List<ElinkRingHistoryData>, total: Int, sentCount: Int) {
        addLog("${getString(R.string.checkup_history_data)}: $histories, $total, $sentCount, ${sentCount < total}")
    }

    override fun onGetDeviceStatus(status: ElinkRingDeviceStatus) {
        addLog("${getString(R.string.device_status)}: $status")
    }

    override fun onGetSensorVersion(version: String) {
        addLog("${getString(R.string.sensor_version)}: $version")
    }

    override fun onGetAutoCheckupStatus(open: Boolean) {
        addLog("${getString(R.string.auto_checkup_state)}: $open")
    }

    override fun onGetCheckupType(type: ElinkCheckupType) {
        addLog("${getString(R.string.checkup_type)}: $type")
    }

    override fun onNotifyHistoryGenerated() {
        addLog(getString(R.string.history_generated))
    }

    override fun onSetUnixTimeResult(success: Boolean) {
        addLog("${getString(R.string.sync_time)}: $success")
    }

    private fun showListDialog(
        title: String,
        items: Array<String>,
        callback: (String, Int) -> Unit,
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(items) { dialog, which ->
                val selectedItem = items[which]
                callback.invoke(selectedItem, which)
                dialog.dismiss()
            }.show()
    }

    private suspend fun copyAssetToCache(assetFileName: String): File? =
        withContext(Dispatchers.IO) {
            val outputFile = File(cacheDir, assetFileName)
            try {
                assets.open(assetFileName).use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        val buffer = ByteArray(4 * 1024) // 4 KB buffer size
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                    }
                }
                return@withContext outputFile
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }

    private suspend fun readBytesFromAssets(fileName: String): ByteArray = withContext(Dispatchers.IO) {
        val inputStream: InputStream = assets.open(fileName)
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(1024)

        var bytesRead: Int
        while (inputStream.read(data, 0, data.size).also { bytesRead = it } != -1) {
            buffer.write(data, 0, bytesRead)
        }

        buffer.close()
        inputStream.close()
        return@withContext buffer.toByteArray()
    }
}