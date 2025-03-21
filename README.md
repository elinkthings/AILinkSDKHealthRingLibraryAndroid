# AILink Health Ring SDK Usage Guide - Android

##[中文](README_CN.md)

Latest version of AILinkSDKRepositoryAndroid: [![](https://jitpack.io/v/elinkthings/AILinkSDKRepositoryAndroid.svg)](https://jitpack.io/#elinkthings/AILinkSDKRepositoryAndroid) [AILinkSDKRepositoryAndroid aar](https://github.com/elinkthings/AILinkSDKRepositoryAndroid/releases)

Latest version of AILinkSDKOtaLibraryAndroid: [![](https://jitpack.io/v/elinkthings/AILinkSDKOtaLibraryAndroid.svg)](https://jitpack.io/#elinkthings/AILinkSDKOtaLibraryAndroid)[AILinkSDKOtaLibraryAndroid aar](https://github.com/elinkthings/AILinkSDKOtaLibraryAndroid/releases)

Latest version of AILinkSDKHealthRingLibraryAndroid: [![](https://jitpack.io/v/elinkthings/AILinkSDKHealthRingLibraryAndroid.svg)](https://jitpack.io/#elinkthings/AILinkSDKHealthRingLibraryAndroid)[AILinkSDKHealthRingLibraryAndroid aar](https://github.com/elinkthings/AILinkSDKHealthRingLibraryAndroid/releases)



Download the Android SDK demo from Git:
```
git clone http://git.elinkthings.com/elink/HealthRing-bleSDK-android-Demo.git
```



##  Requirements
1. Minimum Android SDK version: Android 5.0 (API 21). 
2. Bluetooth version 4.0 or higher is required for the device. 
3. Java version 1.8 is required.


##  Notes

-  For usage of AILinkSDKRepositoryAndroid and AILinkSDKOtaLibraryAndroid, please refer to the [documentation](http://doc.elinkthings.com/web/#/12?page_id=50)
-  For more operations, please refer to the demo.

##  SDK Integration
```
1.Add the JitPack repository to your build file
Add it to the end of the repositories in the root build.gradle:
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

2.Add dependencies, refer to the latest version in the documentation header
	dependencies {
	        implementation 'com.github.elinkthings:AILinkSDKRepositoryAndroid:Tag'//Bluetooth library (required)
            implementation 'com.github.elinkthings:AILinkSDKOtaLibraryAndroid:Tag'//OTA library (required)
            implementation 'com.github.elinkthings:AILinkSDKHealthRingLibraryAndroid:Tag'//Health ring library (required, depends on Bluetooth library and OTA library)
	}

3.Configure Java 1.8 in Gradle
    android {
        ...
        compileOptions {
            sourceCompatibility 1.8
            targetCompatibility 1.8
        }
		repositories {
			flatDir {
				dirs 'libs'
			}
		}
	}


Alternatively, you can use aar package dependencies by downloading and placing them in the libs directory of your project. Download links are provided at the top of the document.

dependencies {
	        //implementation(name: 'aar包名', ext: 'aar')
	}
```

## Permission settings

```

    <!-- For BLE compatibility on Android 6.0 and above -->
    <uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission-sdk-23 android:name="android.permission.ACCESS_FINE_LOCATION" />
	<!-- For Android 12, add maxSdkVersion -->
    <uses-permission
        android:name="android.permission.BLUETOOTH"
        android:maxSdkVersion="30" />
    <uses-permission
        android:name="android.permission.BLUETOOTH_ADMIN"
        android:maxSdkVersion="30" />
    <!-- For Android 12, additional permissions are required and need to be dynamically requested -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN"
                     android:usesPermissionFlags="neverForLocation"
                     tools:targetApi="s"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <!-- For Android 10 and 11, background scanning requires the following permission -->
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="false" />
    <uses-feature
        android:name="android.hardware.bluetooth"
        android:required="false" />

```

-  <font color="#FF0000">Location permission is mandatory for Android 6.0 and above. Developers need to manually request this permission, otherwise devices may not be discovered (dynamic request for ACCESS_FINE_LOCATION).</font>
-  <font color="#FF0000">For Android 12 and above, location permission is not required, but permission to connect to nearby devices is needed. Developers need to request this permission, otherwise it may cause app crashes (dynamic request for BLUETOOTH_SCAN).</font>

## Getting Started

> Add the following code snippet inside the <application> tag of your AndroidManifest.xml file:

```
<application>
    ...

    <service android:name="com.pingwang.bluetoothlib.server.ELinkBleServer"/>

</application>

```


### SDK Initialization

- <font color="#FF0000">Ensure that initialization is successful before executing other operations, such as setOnCallbackBle and startScan methods.</font>

```

AILinkSDK.getInstance().init(mContextApplication);
 AILinkBleManager.getInstance().init(mContext, new AILinkBleManager.onInitListener() {
            @Override
            public void onInitSuccess() {
                // Initialization successful
            }

            @Override
            public void onInitFailure() {
				// Initialization failed
            }
        });

```

### BLE State Interface (Scan, Connect, Disconnect)

-  Implement the OnCallbackBle interface by setting the interface AILinkBleManager.getInstance().setOnCallbackBle(); to receive status and data such as scanning, connecting, and disconnecting.

```
/**
 * Bluetooth scan, connection, and other operation interface
 */
public interface OnCallbackBle extends OnCallback {
    /**
     * Start scanning for devices
     */
    default void onStartScan(){}
    /**
     * Callback for each scanned device
     */
    default void onScanning(BleValueBean data){}
    /**
     * Scan timeout (complete)
     */
   default void onScanTimeOut(){}
   

	
	/**
     * Scan error
     *
     * @param time Time after which scanning can be performed again
     * @param type 类型 {@link com.pingwang.bluetoothlib.config.BleConfig#SCAN_FAILED_SCANNING_TOO_FREQUENTLY} Too frequent
     *             {@link com.pingwang.bluetoothlib.config.BleConfig#SCAN_FAILED_TOO_THREE} Scanning failed more than 3 times
     *             {@link com.pingwang.bluetoothlib.config.BleConfig#SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES} Hardware not supported (may also be a permission issue)
     */
    default void onScanErr(int type, long time){}
   
    /**
     * Connecting
     */
   default void onConnecting(String mac){}
   /**
     * Connection disconnected, in the UI thread
     * @param mac  Mac address
     * @param code -1 Connection timeout, other error codes please refer to BLE protocol stack
     */
   default void onDisConnected(String mac, int code){}

    /**
     * Connection successful (service discovered), in the UI thread
     */
  default void onServicesDiscovered(String mac){}

    /**
     * Bluetooth is turned on, triggered on the thread
     */
   default void bleOpen(){}

    /**
     * Bluetooth is not turned on, triggered on the thread
     */
   default void bleClose(){}
}
```

### Device Scanning

-  Start scanning: AILinkBleManager.getInstance().startScan(long timeOut); (timeOut in milliseconds)

```
      /**
       * Search for devices (no filtering)
       * Scanning too frequently may cause scanning failure
       * Ensure that the total scanning time of 5 scans exceeds 30s
       * @param timeOut Timeout, in milliseconds (how long to search for data, 0 means continuous scanning)
       */
     startScan(long timeOut)

      /**
       * Search for devices
       * Scanning too frequently may cause scanning failure
       * Ensure that the total scanning time of 5 scans exceeds 30s
       * @param timeOut  Timeout, in milliseconds (how long to search for data, 0 means continuous scanning)
       * @param scanUUID UUID to filter (an empty array means no filtering)
       */
     startScan(long timeOut, UUID scanUUID)

	Example: Searching for AILink devices only
    // BleConfig.UUID_SERVER_AILINK is the connection class device for AILink
    // BleConfig.UUID_SERVER_BROADCAST_AILINK is the broadcast class device for AILink
     startScan(30000, BleConfig.UUID_SERVER_AILINK,BleConfig.UUID_SERVER_BROADCAST_AILINK)
```

- Scanned device callback: You can obtain it in the OnCallbackBle interface or OnScanFilterListener interface.

```

//OnCallbackBle interface
onScanning(BleValueBean data){
// Returns each scanned device, can be used to get broadcast data and refresh signal value
// Developers need to filter and deduplicate by themselves (can be deduplicated by MAC address)

}


// OnScanFilterListener interface
// It is recommended to implement this interface when you don't need to connect, which can reduce the number of interface implementations
onScanRecord(BleValueBean bleValueBean){
// Returns each scanned device, can be used to get broadcast data and refresh signal value
// Developers need to filter and deduplicate by themselves (can be deduplicated by MAC address)
}

/**
 * Filter calculation -> can filter and select broadcast data
 * @param bleValueBean Bluetooth broadcast data
 * @return Whether it is valid, true for valid, false for invalid, discarded
 */
onFilter(BleValueBean bleValueBean){

}

```

### Connecting to a Device

-  Connect: AILinkBleManager.getInstance().connectDevice(BleValueBean bleValueBean); or connectDevice(String mAddress);

```
// Note: It is recommended to stop scanning before connecting to make the connection process more stable
AILinkBleManager.getInstance().stopScan()


// Connection successful and service discovery will be called back in the OnCallbackBle interface
onServicesDiscovered(String mac){
   // Connection successful, and service UUID is obtained successfully
   // Data transmission and reception can be performed
}

```

### Disconnecting
```
// Disconnect all connections, AILink library supports connecting multiple Bluetooth devices.
AILinkBleManager.getInstance().disconnectAll();

// The AILinkBleManager object provides only the method to disconnect all devices. To disconnect a specific device, you can use the BleDevice.disconnect() method.
// You can get the BleDevice object in this way
AILinkBleManager.getInstance().getBleDevice(String mac);
```


### Sending Data

- After obtaining the connection object, you can send and receive data from the device.

``` kotlin
// The BleDevice object has all operations on this device, including disconnecting, sending commands, and receiving commands.
val bleDevice = AILinkBleManager.getInstance().getBleDevice(mAddress);
val ringBleData: ElinkHealthRingBleData = ElinkHealthRingBleData(bleDevice)

//Query device status
ringBleData.getDeviceState()

//Get daily monitoring period
ringBleData.getCheckupDuration()

//Get sensor version
ringBleData.getSensorVersion()

//Get daily monitoring mode
ringBleData.getCheckupType()

//Get daily monitoring status
ringBleData.getAutoCheckState()

//Start daily monitoring
ringBleData.openAutoCheck()

//Stop daily monitoring
ringBleData.closeAutoCheck()

//Stop daily monitoring
ringBleData.setCheckupDuration(duration: Int) //Unit: Minutes

//Set daily monitoring mode
ringBleData.setCheckupType(type: ElinkCheckupType) //ElinkCheckupType.COMPLEX: Comprehensive monitoring, ElinkCheckupType.FAST: Quick monitoring

//Get history data
ringBleData.getHistory()

//Get the next page of history data
ringBleData.getNextHistory()

//End of history data
ringBleData.getHistoryOver()

//Delete history data
ringBleData.deleteHistory()

//Start health checkup
ringBleData.startCheckup()

//End health checkup
ringBleData.stopCheckup()

//Synchronize Unix time. Note that the timestamp here must be consistent with the timestamp of syncBleTime
ringBleData.syncUnixTime(timestamp: Long)

//Synchronize BLE system time. Note that the timestamp here must be consistent with the timestamp of syncUnixTime
ringBleData.syncBleTime(timestamp: Long)

//Set the sleep/step monitoring cycle
ringBleData.setSleepAndStepDuration(duration: Int = 5) //Unit: minutes

//Query the sleep/step monitoring cycle
ringBleData.querySleepAndStepDuration()

//Query the sleep monitoring status
ringBleData.querySleepCheck()

//Open sleep monitoring
ringBleData.openSleepCheck()

//Close sleep monitoring
ringBleData.closeSleepCheck()

//Query the step monitoring status
ringBleData.queryStepCheck()

//Open step monitoring
ringBleData.openStepCheck()

//Close step monitoring
ringBleData.closeStepCheck()

//Get sleep/step history data
ringBleData.getSleepAndStepHistory()

//Get the next page of sleep/step history data
ringBleData.getNextSleepAndStepHistory()

//Get the end of sleep/step history data
ringBleData.getSleepAndStepHistoryOver()

//Delete sleep/step history data
ringBleData.deleteSleepAndStepHistory()

//Sensor OTA
ringBleData.startSensorOTA(fileData: ByteArray)

//Sensor OTA callback
ringBleData.setImplSensorOTA(object : ImplSensorOTA {
    override fun onFailure(type: ElinkSensorOTAErrorType) {}
    override fun onSuccess() {}
    override fun onProgress(progress: Int) {}
})

//Bluetooth OTA
ringBleData.startBleOTA(filePath: String, object : OnBleOTAListener {
    override fun onOtaSuccess() {}
    override fun onOtaFailure(cmd: Int, err: String?) {}
    override fun onOtaProgress(progress: Float, currentCount: Int, maxCount: Int,) {}
    override fun onOtaStatus(status: Int) {}
    override fun onReconnect(mac: String?) {}
})


//Disconnect the connection of the current device
BleDevice.disconnect();

```

- <font color="#FF0000">The timestamp parameter of ringBleData.syncUnixTime(timestamp: Long) and ringBleData.syncBleTime(timestamp: Long) must use the same value</font>

### Device Command Reply Callback Explanation
```kotlin
ringBleData?.setImplHealthRingResult(object : ImplHealthRingResult {
   
   /**
    * Callback for starting health checkup
    * 
    * @param success result
    */
   override fun startCheckup(success: Boolean) {}
   
   /**
    * Callback for ending health checkup
    * 
    * @param success result
    */
   override fun stopCheckup(success: Boolean) {}
   
   /**
    * Callback for real-time health checkup data
    * 
    * @param data ElinkCheckupRealtimeData
    */
   override fun onGetRealtimeData(data: ElinkCheckupRealtimeData) {}
   
   /**
    * Health checkup packet callback
    * 
    * @param data ByteArray
    */
   override fun onGetCheckupPackets(data: ByteArray) {}
   
   /**
    * Callback for querying and setting daily monitoring period
    * 
    * @param duration Unit (minutes)
    */
   override fun onGetCheckupDuration(duration: Int) {}

   /**
    * Callback for obtaining history data, judge whether there is more history data based on total and sentCount
    * If there is, call getNextHistory()
    * If not, call getHistoryOver()
    * After the end of history data acquisition, call deleteHistory() to delete history data
    *
    * @param histories List of historical data
    * @param total Total
    * @param sentCount Already acquired count
    */
   override fun onGetHistory(
      histories: List<ElinkRingHistoryData>,
      total: Int,
      sentCount: Int,
   ) {}
   
   /**
    * Callback for obtaining device status
    * 
    * @param status
    */
   override fun onGetDeviceStatus(status: ElinkRingDeviceStatus) {}
   
   /**
    * Callback for obtaining sensor version
    * 
    * @param version
    */
   override fun onGetSensorVersion(version: String) {}
   
   /**
    * Callback for querying, starting, and stopping daily monitoring status
    * 
    * @param open
    */
   override fun onGetAutoCheckupStatus(open: Boolean) {}
   
   /**
    * Callback for querying and setting daily monitoring mode
    *
    * @param type ElinkCheckupType.COMPLEX: Comprehensive monitoring, ElinkCheckupType.FAST: Quick monitoring
    */
   override fun onGetCheckupType(type: ElinkCheckupType) {}

   /**
    * Notification callback for generating historical data on the device
    */
   override fun onNotifyHistoryGenerated() {}

   /**
    * Callback for synchronizing UnixTime
    * 
    * @param success
    */
   override fun onSetUnixTimeResult(success: Boolean) {}
    
   /**
    * Synchronous BleTime callback
    *
    * @param success
    */
   override fun onSyncBleTimeResult(success: Boolean) {}
    
})
```

### Device sleep/step command reply callback description
```kotlin
ringBleData?.setImplSleepAndStepResult(object : ImplSleepAndStepResult {

   /**
    * (Query and set) sleep/step monitoring cycle callback
    *
    * @param duration unit (minutes)
    */
   override fun onGetCheckDuration(duration: Int) {}
    
   /**
    * Get sleep/step history data callback, and compare total and sentCount to determine whether there is more history data
    * If there is, call getNextHistory()
    * If not, call getHistoryOver()
    * After finishing getting history data, call deleteHistory() to delete history data
    *
    * @param histories history data list
    * @param total total
    * @param sentCount received number
    */
   override fun onGetSleepAndStepHistory(
      histories: List<ElinkSleepAndStepData>,
      total: Int,
      sentCount: Int
   ) {}

   /**
    * The device generates a callback for notification of sleep/step history data
    */
   override fun onNotifySleepAndStepHistoryGenerated() {}

   /**
    * (Query, open and close) sleep monitoring status callback
    *
    * @param open
    */
   override fun onGetSleepCheckState(open: Boolean) {}
   
   /**
    * (Query, open and close) step monitoring status callback
    *
    * @param open
    */
   override fun onGetStepCheckState(open: Boolean) {}

})
```

### Related Class Descriptions

#### ElinkCheckupRealtimeData
```kotlin
data class ElinkCheckupRealtimeData(
    val heartRate: Int, //Heart rate
    val bloodOxygen: Int,   //Blood oxygen
    val heartList: List<Int>,   //Heart rhythm
    val rr: Int,
    val rri: List<Int>,
)
```

#### ElinkRingDeviceStatus
```kotlin
data class ElinkRingDeviceStatus(
   val state: ElinkRingHistoryState, //Historical data status
   val batteryLevel: Int, //Battery
   val isCharging: Boolean, //Whether it is charging
   val wearingStatus: ElinkWearingStatus, //wearing status
)
```

#### ElinkRingHistoryState
```kotlin
enum class ElinkRingHistoryState {
   NOT_READY, //The historical time is not ready (unix time is not obtained)
   PROCESSING, //Historical time is being processed (unix time has been obtained and historical data is being processed)
   READY, //The historical time is ready (only in this state can the device history be obtained)
}
```
-  <font color="#FF0000">In the ElinkRingHistoryState.NOT_READY state, you need to call ringBleData.syncUnixTime() to actively synchronize the time, otherwise the historical data time obtained will be wrong</font>

#### ElinkRingHistoryData
```kotlin
data class ElinkRingHistoryData(
    val heartRate: Int, //Heart rate
    val bloodOxygen: Int, //blood oxygen
    val bk: Int, //Microcycle
    val sbp: Int, //systolic blood pressure (high pressure)
    val dbp: Int, //diastolic blood pressure (low pressure)
    val rr: Int, //respiration rate
    val sdann: Int,
    val rmssd: Int,
    val nn50: Int,
    val pnn50: Int,
    val time: Long, //time
    val rri: List<Int>,
)
```

#### ElinkCheckupType
```kotlin
enum class ElinkCheckupType(val size: Byte) {
    COMPLEX(72),    //Comprehensive monitoring
    FAST(30),       //Quick monitoring
}
```

#### ElinkSensorOTAErrorType
```kotlin
enum class ElinkSensorOTAErrorType {
    START_OTA_FAIL,
    CHECK_FAIL,
    WRITE_ERROR,
    ERASE_ERROR,
}
```

#### ElinkWearingStatus
```kotlin
enum class ElinkWearingStatus {
   UNSUPPORTED, //not supported
   NOT_WEARING, //not wearing
   WEARING, //Wearing
}
```

### ElinkSleepState
```kotlin
enum class ElinkSleepState {
    AWAKE,  //Wide Awake
    REM,    //Rapid Eye Movement
    LIGHT,  //Light sleep
    DEEP,   //Deep sleep
}
```

### Front desk service settings (optional)

```
   /**
      *Set front-end service related parameters
      * @param id id
      * @param icon logo
      * @param title title
      * @param activityClass jump activity
      */
   AILinkBleManager.getInstance().initForegroundService(int id, @DrawableRes int icon, String title, Class<?> activityClass)

   //Start the foreground service,
   AILinkBleManager.getInstance().startForeground();

   //Stop the foreground service
   AILinkBleManager.getInstance().stopForeground();
```

- setOnBleVersionListener(OnBleVersionListener bleVersionListener) in BleDevice//Device version number, unit interface

```
   public interface OnBleVersionListener {
     /**
      *BM module software and hardware version number
      */
     default void onBmVersion(String version){}
     /**
      * Units supported by mcu (all supported by default)
      * @param list null or empty means all are supported
      */
     default void onSupportUnit(List<SupportUnitBean> list) {}
}

```

## Version history

- AILinkSDKHealthRingLibraryAndroid library

| Version number | Update time | Author | Update information |
|:---------------|:------------|:------|-----|
| 1.0.0          | 2024/03/14  | suzy | Initial version
| 1.1.0          | 2025/03/21  | suzy | Added sleep and step count command processing
---

## FAQ

- Can't scan for Bluetooth devices?

1. Check whether the App permissions are normal. Systems 6.0 and above must require positioning permissions and need to obtain permissions manually. After android 7.0, you cannot scan + stop more than 5 times within 30 seconds. Searching after android 9.0 requires precise positioning permissions.
2. Check whether the mobile phone’s positioning service is turned on. Some mobile phones may need to turn on GPS;
3. Is ELinkBleServer registered in AndroidManifest;
4. Whether the device is connected to other mobile phones;
5. Whether the search method is called too frequently, the scanLeDevice method needs to ensure that the total duration of 5 scans exceeds 30s (each phone has differences, it is recommended to reduce the frequency as much as possible);
6. Restart the phone’s Bluetooth and try again. Some phones require the entire phone to be restarted;

## SDK obfuscation configuration (proguard-rules.pro)

```
-keep class com.pinwang.ailinkble.**{*;}

-keep class com.pingwang.bluetoothlib.annotation.**{*;}
-keep class com.pingwang.bluetoothlib.bean.**{*;}
-keep class com.pingwang.bluetoothlib.config.**{*;}
-keep class com.pingwang.bluetoothlib.device.**{*;}
-keep class com.pingwang.bluetoothlib.listener.**{*;}

-keep public class com.pingwang.bluetoothlib.AILinkBleManager{*;}
-keep public class com.pingwang.bluetoothlib.AILinkSDK{*;}
-keep public class com.pingwang.bluetoothlib.utils.BleLog{*;}
-keep public class com.pingwang.bluetoothlib.utils.UuidUtils{*;}
-keep public class com.pingwang.bluetoothlib.utils.BleDataUtils{*;}
-keep public class com.pingwang.bluetoothlib.utils.BleCheckUtils{*;}

-keep class cn.net.aicare.algorithmutil.**{*;}
-keep class cn.net.aicare.**{*;}
-keep class com.besthealth.bhBodyComposition120.**{*;}
-keep class com.holtek.**{*;}
-keep class com.elinkthings.toothscore.**{*;}
-keep class cn.net.aicare.modulelibrary.module.**{*;}

-keep class com.elinkthings.healthring.**{*;}

```

## Contact us
Shenzhen ElinkThings Co., Ltd.

Tel: 0755-81773367

WeChat: ElinkThings08

Website: www.elinkthings.com

E-mail: app@elinkthings.com