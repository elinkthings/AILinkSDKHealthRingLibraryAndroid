package com.elinkthings.elinkhealthringsdkdemo

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.elinkthings.healthring.ElinkBaseBleActivity

/**
 * @author suzy
 * @date 2024/3/12 11:31
 **/
abstract class ElinkBasePermissionActivity : ElinkBaseBleActivity() {

    companion object {
        private val LOCATION_PERMISSION = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        private val BLUETOOTH_PERMISSION = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        private const val CODE_PERMISSION = 101
        private const val CODE_LOCATION_SERVICE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "${getString(R.string.app_name)}${BuildConfig.VERSION_NAME}"
        initPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //请求权限被拒绝
        if (requestCode != CODE_PERMISSION) {
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initPermission()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        LOCATION_PERMISSION[0]
                    )
                ) {
                    //权限请求失败，但未选中“不再提示”选项,再次请求
                    ActivityCompat.requestPermissions(this, LOCATION_PERMISSION, CODE_PERMISSION)
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("请求开启定位权限")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定") { _, _ -> startSettingsActivity() }
                        .show()
                }
            }
        } else {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initPermission()
            }
        }
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onPermissionOk()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            checkLocationPermission()
        } else {
            checkBluetoothPermission()
        }
    }

    private fun checkLocationPermission() {
        if (!checkPermissionOk(LOCATION_PERMISSION[0])) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSION, CODE_PERMISSION)
        } else {
            val bleStatus: Boolean = isLocServiceEnable()
            if (!bleStatus) {
                AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("请开启定位服务")
                    .setNegativeButton("取消") { _, _ -> checkBluetoothPermission() }
                    .setPositiveButton("确定") { _, _ -> startLocationActivity() }
                    .show()
            } else {
                onPermissionOk()
            }
        }
    }

    private fun checkBluetoothPermission() {
        if (!checkPermissionOk(BLUETOOTH_PERMISSION[0])) {
            ActivityCompat.requestPermissions(this, BLUETOOTH_PERMISSION, CODE_PERMISSION)
        } else {
            onPermissionOk()
        }
    }

    private fun checkPermissionOk(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_LOCATION_SERVICE) {
            //定位服务页面返回
            initPermission()
        }
    }

    abstract fun onPermissionOk()

    /**
     * 手机是否开启位置服务
     */
    private fun isLocServiceEnable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }

    /**
     * 进入定位服务
     */
    private fun startLocationActivity() {
        val localIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        if (packageManager.resolveActivity(
                localIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            ) != null
        ) {
            startActivityForResult(localIntent, CODE_LOCATION_SERVICE)
        }
    }

    /**
     * 进入应用设置界面
     *
     */
    private fun startSettingsActivity() {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        localIntent.data = Uri.fromParts("package", packageName, null)
        startActivity(localIntent)
    }
}