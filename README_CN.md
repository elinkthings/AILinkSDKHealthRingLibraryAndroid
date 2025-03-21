# AILink健康戒指SDK使用说明 - Android

##[English](README.md)

Latest version of AILinkSDKRepositoryAndroid: [![](https://jitpack.io/v/elinkthings/AILinkSDKRepositoryAndroid.svg)](https://jitpack.io/#elinkthings/AILinkSDKRepositoryAndroid) [AILinkSDKRepositoryAndroid aar](https://github.com/elinkthings/AILinkSDKRepositoryAndroid/releases)

AILinkSDKOtaLibraryAndroid库最新版: [![](https://jitpack.io/v/elinkthings/AILinkSDKOtaLibraryAndroid.svg)](https://jitpack.io/#elinkthings/AILinkSDKOtaLibraryAndroid)[AILinkSDKOtaLibraryAndroid aar](https://github.com/elinkthings/AILinkSDKOtaLibraryAndroid/releases)

AILinkSDKHealthRingLibraryAndroid库最新版: [![](https://jitpack.io/v/elinkthings/AILinkSDKHealthRingLibraryAndroid.svg)](https://jitpack.io/#elinkthings/AILinkSDKHealthRingLibraryAndroid)[AILinkSDKHealthRingLibraryAndroid aar](https://github.com/elinkthings/AILinkSDKHealthRingLibraryAndroid/releases)



Android sdk demo git下载指令
```
git clone http://git.elinkthings.com/elink/HealthRing-bleSDK-android-Demo.git
```



##  使用条件
1. Android SDK最低版本android5.0（API 21）。
2. 设备所使用蓝牙版本需要4.0及以上。
3. 配置java1.8


##  注意事项

-  AILinkSDKRepositoryAndroid库和AILinkSDKOtaLibraryAndroid库使用请[参考文档](http://doc.elinkthings.com/web/#/12?page_id=49)
-  更多操作请参考demo

##  导入SDK


```


1.将JitPack存储库添加到您的构建文件中
将其添加到存储库末尾的root build.gradle中：
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

2.添加依赖项,最新版本号请参考文档开头
	dependencies {
	        implementation 'com.github.elinkthings:AILinkSDKRepositoryAndroid:Tag'//蓝牙库(必须)
			implementation 'com.github.elinkthings:AILinkSDKOtaLibraryAndroid:Tag'//OTA库(必须)
			implementation 'com.github.elinkthings:AILinkSDKHealthRingLibraryAndroid:Tag'//健康戒指库(必须，依赖蓝牙库和OTA库)
	}

3.在gradle中配置java1.8
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


也可以使用aar包依赖,请自行下载放到项目的libs中,下载地址在文档顶部

dependencies {
	        //implementation(name: 'aar包名', ext: 'aar')
	}

```

## 权限设置

```

    <!--兼容6.0以上的手机Ble-->
    <uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission-sdk-23 android:name="android.permission.ACCESS_FINE_LOCATION" />
	<!--android12需要增加maxSdkVersion-->
    <uses-permission
        android:name="android.permission.BLUETOOTH"
        android:maxSdkVersion="30" />
    <uses-permission
        android:name="android.permission.BLUETOOTH_ADMIN"
        android:maxSdkVersion="30" />
    <!--android12还需要增加如下权限,也需求动态申请-->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN"
                     android:usesPermissionFlags="neverForLocation"
                     tools:targetApi="s"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <!--android10,11需要后台扫描的,需要添加如下权限-->
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="false" />
    <uses-feature
        android:name="android.hardware.bluetooth"
        android:required="false" />

```

-  <font color="#FF0000">6.0及以上系统必须要定位权限，需要开发者向系统申请权限,否则会搜索不到设备(动态申请ACCESS_FINE_LOCATION)</font>
-  <font color="#FF0000">12.0及以上系统可以不用定位权限,但是需要连接附近设备权限,需要开发者向系统申请权限,否则会导致闪退(动态申请BLUETOOTH_SCAN)</font>

## 开始集成

> 在AndroidManifest.xml application标签下面增加

```
<application>
    ...

    <service android:name="com.pingwang.bluetoothlib.server.ELinkBleServer"/>

</application>

```


### SDK初始化

- <font color="#FF0000">初始化成功后才可以执行其他操作,例如setOnCallbackBle,startScan 等方法</font>

```

AILinkSDK.getInstance().init(mContextApplication);
 AILinkBleManager.getInstance().init(mContext, new AILinkBleManager.onInitListener() {
            @Override
            public void onInitSuccess() {
                //初始化成功
            }

            @Override
            public void onInitFailure() {
				//初始化失败
            }
        });

```

### BLE状态接口(搜索,连接,断开)

-  设置接口AILinkBleManager.getInstance().setOnCallbackBle();实现OnCallbackBle接口可以获取搜索,连接,断开等状态和数据

```
/**
 * 蓝牙搜索,连接等操作接口
 */
public interface OnCallbackBle extends OnCallback {
    /**
     * 开始扫描设备
     */
    default void onStartScan(){}
    /**
     * 每扫描到一个设备就会回调一次
     */
    default void onScanning(BleValueBean data){}
    /**
     * 扫描超时(完成)
     */
   default void onScanTimeOut(){}
   

	
	/**
     * 扫描异常
     *
     * @param time 多少ms后才可以再次进行扫描
     * @param type 类型 {@link com.pingwang.bluetoothlib.config.BleConfig#SCAN_FAILED_SCANNING_TOO_FREQUENTLY} 太频繁
     *             {@link com.pingwang.bluetoothlib.config.BleConfig#SCAN_FAILED_TOO_THREE} 扫描失败超过3次
     *             {@link com.pingwang.bluetoothlib.config.BleConfig#SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES} 硬件不支持(也可能是权限问题)
     */
    default void onScanErr(int type, long time){}
   
    /**
     * 正在连接
     */
   default void onConnecting(String mac){}
  /**
     * 连接断开,在UI线程
     * @param mac  mac地址
     * @param code -1连接超时,其他错误码请参考BLE协议栈
     */
   default void onDisConnected(String mac, int code){}

    /**
     * 连接成功(发现服务),在UI线程
     */
  default void onServicesDiscovered(String mac){}

    /**
     * 已开启蓝牙,在触发线程
     */
   default void bleOpen(){}

    /**
     * 未开启蓝牙,在触发线程
     */
   default void bleClose(){}
}
```

### 扫描设备

-  搜索  AILinkBleManager.getInstance().startScan(long timeOut);//timeOut(毫秒)

```
    /**
     * 搜索设备(不过滤)
     * 扫描过于频繁会导致扫描失败
     * 需要保证5次扫描总时长超过30s
     * @param timeOut 超时时间,毫秒(搜索多久去取数据,0代表一直搜索)
     */
     startScan(long timeOut)

   /**
     * 搜索设备
     * 扫描过于频繁会导致扫描失败
     * 需要保证5次扫描总时长超过30s
     * @param timeOut  超时时间,毫秒(搜索多久去取数据,0代表一直搜索)
     * @param scanUUID 过滤的UUID(空数组代表不过滤)
     */
     startScan(long timeOut, UUID scanUUID)

	例:只搜索AILink的设备
	//BleConfig.UUID_SERVER_AILINK是AILink的连接类设备
	//BleConfig.UUID_SERVER_BROADCAST_AILINK是AILink的广播类设备
     startScan(30000, BleConfig.UUID_SERVER_AILINK,BleConfig.UUID_SERVER_BROADCAST_AILINK)


```

- 扫描的设备回调,可以在OnCallbackBle接口或者OnScanFilterListener接口中获取

```

//OnCallbackBle接口
onScanning(BleValueBean data){
//每扫描到设备就会从这个返回,可以用于获取广播数据以及刷新信号值
//需要开发者自己过滤去重(可以通过mac地址去重)

}


//OnScanFilterListener接口
//建议在不需要连接的时候实现此接口,可以减少接口实现
onScanRecord(BleValueBean bleValueBean){
//每扫描到设备就会从这个返回,可以用于获取广播数据以及刷新信号值
//需要开发者自己过滤去重(可以通过mac地址去重)
}

/**
     * 过滤计算->可以对广播数据进行帅选过滤
     *
     * @param bleValueBean 蓝牙广播数据
     * @return 是否有效，true有效，false无效，丢弃
     */
onFilter(BleValueBean bleValueBean){

}

```

### 连接设备

-  连接AILinkBleManager.getInstance().connectDevice(BleValueBean bleValueBean);或者connectDevice(String mAddress);

```
//注:连接之前建议停止搜索,这样连接过程会更稳定
AILinkBleManager.getInstance().stopScan()


//连接成功并获取服务成功后会在OnCallbackBle接口中回调
onServicesDiscovered(String mac){
//连接成功,并且获取服务UUID成功
//可以进行发送和接收数据了
}

```

### 断开连接


```
//断开所有连接,AILink库支持连接多个蓝牙设备.
AILinkBleManager.getInstance().disconnectAll();


//所以AILinkBleManager对象只提供断开所有设备的方法,断开某个设备可用BleDevice.disconnect();方法断开连接
//可以通过这样获取BleDevice对象
AILinkBleManager.getInstance().getBleDevice(String mac);

```


### 发送数据

- 获取到连接对象后可以对设备进行收发数据

``` kotlin
//BleDevice对象拥有对此设备的所有操作,包括断开连接,发送指令,接收指令等操作
val bleDevice = AILinkBleManager.getInstance().getBleDevice(mAddress);
val ringBleData: ElinkHealthRingBleData = ElinkHealthRingBleData(bleDevice)

//查询设备状态
ringBleData.getDeviceState()

//获取日常监测周期
ringBleData.getCheckupDuration()

//获取版本信息
ringBleData.getSensorVersion()

//获取日常监测模式
ringBleData.getCheckupType()

//获取日常监测状态
ringBleData.getAutoCheckState()

//开启日常监测
ringBleData.openAutoCheck()

//关闭日常监测
ringBleData.closeAutoCheck()

//设置日常监测周期
ringBleData.setCheckupDuration(duration: Int) //单位: 分钟

//设置日常监测模式
ringBleData.setCheckupType(type: ElinkCheckupType) //ElinkCheckupType.COMPLEX: 全面监测, ElinkCheckupType.FAST: 快速监测

//获取历史数据
ringBleData.getHistory()

//获取下一页历史数据
ringBleData.getNextHistory()

//获取历史数据结束
ringBleData.getHistoryOver()

//删除历史数据
ringBleData.deleteHistory()

//开始体检
ringBleData.startCheckup()

//结束体检
ringBleData.stopCheckup()

//同步Unix时间，注意此处的timestamp必须和syncBleTime的timestamp保持一致
ringBleData.syncUnixTime(timestamp: Long)

//同步BLE系统时间，注意此处的timestamp必须和syncUnixTime的timestamp保持一致
ringBleData.syncBleTime(timestamp: Long)

//设置睡眠/步数监测周期
ringBleData.setSleepAndStepDuration(duration: Int = 5) //单位: 分钟

//查询睡眠/步数监测周期
ringBleData.querySleepAndStepDuration()

//查询睡眠监测状态
ringBleData.querySleepCheck()

//打开睡眠监测
ringBleData.openSleepCheck()

//关闭睡眠监测
ringBleData.closeSleepCheck()

//查询步数监测状态
ringBleData.queryStepCheck()

//打开步数监测
ringBleData.openStepCheck()

//关闭步数监测
ringBleData.closeStepCheck()

//获取睡眠/步数历史数据
ringBleData.getSleepAndStepHistory()

//获取睡眠/步数下一页历史数据
ringBleData.getNextSleepAndStepHistory()

//获取睡眠/步数历史数据结束
ringBleData.getSleepAndStepHistoryOver()

//删除睡眠/步数历史数据
ringBleData.deleteSleepAndStepHistory()

//传感器OTA
ringBleData.startSensorOTA(fileData: ByteArray)

//传感器OTA回调
ringBleData.setImplSensorOTA(object : ImplSensorOTA {
    override fun onFailure(type: ElinkSensorOTAErrorType) {}
    override fun onSuccess() {}
    override fun onProgress(progress: Int) {}
})

//蓝牙OTA
ringBleData.startBleOTA(filePath: String, object : OnBleOTAListener {
    override fun onOtaSuccess() {}
    override fun onOtaFailure(cmd: Int, err: String?) {}
    override fun onOtaProgress(progress: Float, currentCount: Int, maxCount: Int,) {}
    override fun onOtaStatus(status: Int) {}
    override fun onReconnect(mac: String?) {}
})


//断开当前设备的连接
BleDevice.disconnect();

```
-  <font color="#FF0000">ringBleData.syncUnixTime(timestamp: Long)和ringBleData.syncBleTime(timestamp: Long)的入参timestamp须使用同一个值</font>

### 设备指令回复回调说明
```kotlin
ringBleData?.setImplHealthRingResult(object : ImplHealthRingResult {
   
   /**
    * 开始体检回调
    * 
    * @param success 结果
    */
   override fun startCheckup(success: Boolean) {}
   
   /**
    * 结束体检回调
    * 
    * @param success 结果
    */
   override fun stopCheckup(success: Boolean) {}
   
   /**
    * 体检实时数据回调
    * 
    * @param data ElinkCheckupRealtimeData
    */
   override fun onGetRealtimeData(data: ElinkCheckupRealtimeData) {}
   
   /**
    * 体检包回调
    * 
    * @param data ByteArray
    */
   override fun onGetCheckupPackets(data: ByteArray) {}
   
   /**
    * (查询和设置)日常监测周期回调
    * 
    * @param duration 单位(分钟)
    */
   override fun onGetCheckupDuration(duration: Int) {}
   
   /**
    * 获取历史数据回调，根据total和sentCount对比判断是否有更多历史数据
    * 如果有就调用getNextHistory()
    * 如果没有就调用getHistoryOver()
    * 结束获取历史数据后调用deleteHistory()删除历史数据
    * 
    * @param histories 历史数据列表
    * @param total 总数
    * @param sentCount 已获取数
    */
   override fun onGetHistory(
      histories: List<ElinkRingHistoryData>,
      total: Int,
      sentCount: Int,
   ) {}
   
   /**
    * 获取设备状态回调
    * 
    * @param status
    */
   override fun onGetDeviceStatus(status: ElinkRingDeviceStatus) {}
   
   /**
    * 获取传感器版本回调
    * 
    * @param version
    */
   override fun onGetSensorVersion(version: String) {}
   
   /**
    * (查询、开启和关闭)日常监测状态回调
    * 
    * @param open
    */
   override fun onGetAutoCheckupStatus(open: Boolean) {}
   
   /**
    * (查询和设置)日常监测模式回调
    *
    * @param type ElinkCheckupType.COMPLEX: 全面监测, ElinkCheckupType.FAST: 快速监测
    */
   override fun onGetCheckupType(type: ElinkCheckupType) {}

   /**
    * 设备产生历史数据通知回调
    */
   override fun onNotifyHistoryGenerated() {}

   /**
    * 同步UnixTime回调
    * 
    * @param success
    */
   override fun onSetUnixTimeResult(success: Boolean) {}

   /**
    * 同步BleTime回调
    *
    * @param success
    */
   override fun onSyncBleTimeResult(success: Boolean) {}

})
```

### 设备睡眠/步数指令回复回调说明
```kotlin
ringBleData?.setImplSleepAndStepResult(object : ImplSleepAndStepResult {

   /**
    * (查询和设置)睡眠/步数监测周期回调
    *
    * @param duration 单位(分钟)
    */
   override fun onGetCheckDuration(duration: Int) {}

   /**
    * 获取睡眠/步数历史数据回调，根据total和sentCount对比判断是否有更多历史数据
    * 如果有就调用getNextHistory()
    * 如果没有就调用getHistoryOver()
    * 结束获取历史数据后调用deleteHistory()删除历史数据
    *
    * @param histories 历史数据列表
    * @param total 总数
    * @param sentCount 已获取数
    */
   override fun onGetSleepAndStepHistory(
      histories: List<ElinkSleepAndStepData>,
      total: Int,
      sentCount: Int
   ) {}

   /**
    * 设备产生睡眠/步数历史数据通知回调
    */
   override fun onNotifySleepAndStepHistoryGenerated() {}

   /**
    * (查询、开启和关闭)睡眠监测状态回调
    *
    * @param open
    */
   override fun onGetSleepCheckState(open: Boolean) {}
   
   /**
    * (查询、开启和关闭)步数监测状态回调
    *
    * @param open
    */
   override fun onGetStepCheckState(open: Boolean) {}

})
```

### 相关类说明

#### ElinkCheckupRealtimeData
```kotlin
data class ElinkCheckupRealtimeData(
    val heartRate: Int, //心率
    val bloodOxygen: Int,   //血氧
    val heartList: List<Int>,   //心律
    val rr: Int,
    val rri: List<Int>,
)
```

#### ElinkRingDeviceStatus
```kotlin
data class ElinkRingDeviceStatus(
    val state: ElinkRingHistoryState, //历史数据状态
    val batteryLevel: Int,  //电量
    val isCharging: Boolean,    //是否在充电
    val wearingStatus: ElinkWearingStatus,  //佩戴状态
)
```

#### ElinkRingHistoryState
```kotlin
enum class ElinkRingHistoryState {
    NOT_READY, //历史时间未就绪(未获取unix时间)
    PROCESSING, //历史时间正在处理中(已获取unix时间,在处理历史数据)
    READY,  //历史时间已就绪(此状态才可获取设备历史记录)
}
```
-  <font color="#FF0000">ElinkRingHistoryState.NOT_READY状态下，需要调用ringBleData.syncUnixTime()主动同步时间，不然获取到的历史数据时间会是错误的</font>

#### ElinkRingHistoryData
```kotlin
data class ElinkRingHistoryData(
    val heartRate: Int, //心率
    val bloodOxygen: Int,   //血氧
    val bk: Int,    //微循环
    val sbp: Int,   //收缩压(高压)
    val dbp: Int,   //舒张压(低压)
    val rr: Int,    //呼吸率
    val sdann: Int,
    val rmssd: Int,
    val nn50: Int,
    val pnn50: Int,
    val time: Long, //时间
    val rri: List<Int>,
)
```

#### ElinkCheckupType
```kotlin
enum class ElinkCheckupType(val size: Byte) {
    COMPLEX(72),    //全面体检
    FAST(30),       //快速体检
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
    UNSUPPORTED, //不支持
    NOT_WEARING,  //未佩戴
    WEARING,  //佩戴中
}
```

### ElinkSleepState
```kotlin
enum class ElinkSleepState {
    AWAKE,  //清醒
    REM,    //快速眼动
    LIGHT,  //浅睡
    DEEP,   //深睡
}
```

### 前台服务设置(非必须)

```
    /**
     * 设置前台服务相关参数
     * @param id id
     * @param icon logo
     * @param title 标题
     * @param activityClass 跳转的activity
     */
AILinkBleManager.getInstance().initForegroundService(int id, @DrawableRes int icon, String title, Class<?> activityClass)

//启动前台服务,
AILinkBleManager.getInstance().startForeground();

//停止前台服务
AILinkBleManager.getInstance().stopForeground();
```

-  BleDevice 中的setOnBleVersionListener(OnBleVersionListener bleVersionListener)//设备版本号,单位接口

```
  public interface OnBleVersionListener {
    /**
     * BM 模块软、硬件版本号
     */
    default void onBmVersion(String version){}
    /**
     * mcu 支持的单位(默认支持所有)
     * @param list null或者空代表支持所有
     */
    default void onSupportUnit(List<SupportUnitBean> list) {}
}

```

## 版本历史

- AILinkSDKHealthRingLibraryAndroid库

| 版本号    | 更新时间        | 作者    |更新信息|
|:-------|:------------|:------|-----|
| 1.0.0  | 	2024/03/14 | 	suzy |	初始版本
---

## FAQ

- 扫描不到蓝牙设备？

1. 查看App权限是否正常,6.0及以上系统必须要定位权限，且需要手动获取权限,android 7.0 后不能在30秒内扫描+停止超过5次,android 9.0以后搜索需要精准定位权限
2. 查看手机的定位服务是否开启,部分手机可能需要打开GPS;
3. ELinkBleServer是否在在AndroidManifest中注册;
4. 设备是否被其他手机连接;
5. 是否调用搜索方法太频繁, scanLeDevice方法需要保证5次扫描总时长超过30s(各别手机有差异,建议尽量减少频率);
6. 重启手机蓝牙再试试,部分手机需要整个手机重启;

---

- SDK搜索不到其他第三方设备?

1. scanLeDevice(long timeOut, UUID... scanUUID); scanUUID写成第三方的广播UUID或者不写

---


```
在gradle中配置java1.8
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
```

---

-  java.lang.UnsatisfiedLinkError: dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/aicare.net.cn.sdk.ailinksdkdemoandroid-duDywZpVbVzKW4Q18GgfFA==/base.apk"],nativeLibraryDirectories=[/dat
   ![](http://doc.elinkthings.com/server/../Public/Uploads/2021-08-04/610a331294e02.png)

---

- SDK连接第三方设备发送后断开或者发送不了数据?

1. 联系商务

## SDK混淆配置(proguard-rules.pro)

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


## 联系我们
深圳市易连物联网有限公司

电话：0755-81773367

微信号:ElinkThings08

官网：www.elinkthings.com

邮箱：app@elinkthings.com