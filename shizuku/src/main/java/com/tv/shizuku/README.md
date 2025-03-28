### 2. 配置 AndroidManifest.xml

在 `AndroidManifest.xml` 中添加 ShizukuProvider：

```xml

<provider android:name="rikka.shizuku.ShizukuProvider"
    android:authorities="${applicationId}.shizuku" android:enabled="true" android:exported="true"
    android:multiprocess="false"
    android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
```

请将此 provider 添加到 `<application>` 标签内。

## 使用指南

### 初始化 ShizukuManager

在 Application 或主 Activity 的 onCreate 中初始化：

```kotlin
// 在 Application.onCreate 中
override fun onCreate() {
    super.onCreate()
    ShizukuManager.init()
}

// 或在 Activity.onCreate 中
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ShizukuManager.init()
}

// 在 Activity.onDestroy 或 Application 中适当位置释放资源
override fun onDestroy() {
    super.onDestroy()
    ShizukuManager.release()
}
```

### 请求权限

在需要使用 Shizuku 功能的地方请求权限：

```kotlin
ShizukuManager.requestPermission(object : ShizukuManager.PermissionListener {
    override fun onPermissionGranted() {
        // 权限已授予，可以绑定服务
        ShizukuManager.bindService(context)
    }

    override fun onPermissionDenied() {
        // 权限被拒绝，提示用户
        Toast.makeText(context, "需要 Shizuku 权限才能继续操作", Toast.LENGTH_SHORT).show()
    }
})
```

### 执行命令

在获取权限并绑定服务后，可以执行命令：

```kotlin
// 检查 Shizuku 服务是否已连接
if (ShizukuManager.isConnected()) {
    // 执行命令
    val result = ShizukuManager.exec("pm list packages")
    Log.d("Shizuku", "命令执行结果: $result")
} else {
    // 绑定服务
    ShizukuManager.bindService(context)
    // 监听服务连接状态
    ShizukuManager.addConnectionListener(object : ShizukuManager.ConnectionListener {
        override fun onServiceConnected() {
            // 服务已连接，可以执行命令
            val result = ShizukuManager.exec("pm list packages")
            Log.d("Shizuku", "命令执行结果: $result")
        }

        override fun onServiceDisconnected() {
            // 服务已断开
            Log.d("Shizuku", "服务连接断开")
        }
    })
}
```

### 常用命令示例

```kotlin
// 打开/关闭 WiFi
ShizukuManager.exec("svc wifi enable")  // 打开 WiFi
ShizukuManager.exec("svc wifi disable")  // 关闭 WiFi

// 获取 WiFi 状态
val wifiStatus = ShizukuManager.exec("cmd wifi status")

// 安装/卸载应用
ShizukuManager.exec("pm install -r /sdcard/shizuku.apk")
ShizukuManager.exec("pm uninstall com.package.name")

// 授予权限
ShizukuManager.exec("pm grant com.package.name android.permission.PERMISSION_NAME")

// 启动设置页面
ShizukuManager.exec("am start -a android.settings.WIFI_SETTINGS")
```