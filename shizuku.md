## 一、概述

Shizuku 是一个在非 Root 环境下通过 ADB 或 Root 授权方式获取部分系统权限的工具。本文档详细说明如何在应用中接入 Shizuku 服务，以实现与系统应用相同级别的权限管理。

## 二、前置条件

1. 用户需要安装 [Shizuku 应用](https://github.com/RikkaApps/Shizuku)
2. Shizuku 应用需要通过以下任一方式启动:
   - 通过 ADB 命令一次性开启
   - 在已 Root 的设备上持久化运行
   - 某些定制 ROM 上通过无线调试功能启动

## 三、依赖配置

### 1. 添加依赖 

在项目的 `gradle/libs.versions.toml` 文件中添加 Shizuku 相关依赖：

```toml
[versions]
shizuku_api = "13.1.5"  # 最新版本请查看 GitHub 仓库

[libraries]
shizuku-api = { module = "dev.rikka.shizuku:api", version.ref = "shizuku_api" }
shizuku-provider = { module = "dev.rikka.shizuku:provider", version.ref = "shizuku_api" }
```

在应用模块的 `build.gradle.kts` 中添加依赖并启用 aidl：

```kotlin
buildFeatures {
    aidl = true
    // ...
}
```

```kotlin
dependencies {
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
}
```

## 四、清单文件配置

在 `AndroidManifest.xml` 中添加 ShizukuProvider：

```xml
<provider
    android:name="rikka.shizuku.ShizukuProvider"
    android:authorities="${applicationId}.shizuku"
    android:enabled="true"
    android:exported="true"
    android:multiprocess="false"
    android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
```

## 五、Shizuku 权限请求

### 1. 检查和请求 Shizuku 权限

```kotlin
private fun checkShizukuPermission() {
    try {
        // 检查是否已经授予权限
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // 已有权限，可以继续操作
            Log.d("Shizuku", "已授予 Shizuku 权限")
            return
        }

        // 检查 Shizuku 版本是否支持权限请求
        if (Shizuku.isPreV11()) {
            // 旧版 Shizuku，无法通过标准方式请求权限
            Log.e("Shizuku", "Shizuku 版本过低，请更新")
            return
        }
        
        // 请求权限，REQUEST_CODE 可以自定义
        Shizuku.requestPermission(REQUEST_CODE)
    } catch (e: Exception) {
        // 可能 Shizuku 服务未运行
        Toast.makeText(this, "请检查 Shizuku 是否运行", Toast.LENGTH_SHORT).show()
        Log.e("Shizuku", "权限检查异常: ${e.message}")
    }
}
```

### 2. 处理权限请求结果

在 Activity 中添加权限结果处理：

```kotlin
// 权限请求回调监听器
private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
    if (requestCode == REQUEST_CODE) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            // 已获得权限，可以继续操作
            Toast.makeText(this, "Shizuku 权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            // 用户拒绝了权限请求
            Toast.makeText(this, "Shizuku 权限被拒绝", Toast.LENGTH_SHORT).show()
        }
    }
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // 注册权限回调监听器
    Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
}

override fun onDestroy() {
    super.onDestroy()
    
    // 移除监听器，避免内存泄漏
    Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
}
```

## 六、使用 Shizuku UserService

UserService 是一个在 Shizuku 进程内运行的服务，它可以使用 Shizuku 的权限执行操作。

### 1. 定义 AIDL 接口

在 `app/src/main/aidl/com/your/package/IUserService.aidl` 中定义接口：

```java
package com.your.package;

interface IUserService {
    // 销毁服务
    void destroy() = 16777114;  // 固定的方法编号，由 Shizuku 规定

    // 自定义方法
    void exit() = 1;
    
    // 执行 shell 命令
    String exec(String command) = 2;
    
    // 可以添加更多自定义方法
}
```

### 2. 实现 UserService

在 Java 或 Kotlin 中实现该接口：

**Java 示例**:

```java
package com.your.package;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserService extends IUserService.Stub {
    @Override
    public void destroy() {
        System.exit(0);
    }

    @Override
    public void exit() {
        destroy();
    }

    @Override
    public String exec(String command) {
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }
}
```

**Kotlin 示例**:

```kotlin
package com.your.package

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess

class UserService : IUserService.Stub() {
    override fun destroy() = 
        exitProcess(0)

    override fun exit() = 
        destroy()

    override fun exec(command: String): String {
        val result = StringBuilder()
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                result.append(line).append("\n")
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return result.toString()
    }
}
```

### 3. 启动和绑定 UserService

```kotlin
private fun startUserService() {
    if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
        // 如果没有权限，请先请求权限
        checkShizukuPermission()
        return
    }
    
    try {
        // 创建 UserService 参数
        val userServiceArgs = UserServiceArgs(
            ComponentName(
                BuildConfig.APPLICATION_ID,
                UserService::class.java.name
            )
        )
            .daemon(false)  // 是否作为守护进程运行
            .processNameSuffix("service")  // 进程名称后缀
            .debuggable(BuildConfig.DEBUG)  // 是否可调试
            .version(BuildConfig.VERSION_CODE)  // 版本号
        
        // 创建服务连接回调
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Toast.makeText(this@MainActivity, "服务连接成功", Toast.LENGTH_SHORT).show()
                
                // 检查 Binder 是否有效
                if (service.pingBinder()) {
                    // 获取接口代理对象
                    val userService = IUserService.Stub.asInterface(service)
                    try {
                        // 执行需要系统权限的命令，例如:
                        val result = userService.exec("pm list packages")
                        Log.d("UserService", "执行结果: $result")
                    } catch (e: RemoteException) {
                        Log.e("UserService", "执行命令失败", e)
                    }
                }
            }
            
            override fun onServiceDisconnected(name: ComponentName) {
                Toast.makeText(this@MainActivity, "服务连接断开", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 绑定 UserService
        Shizuku.bindUserService(userServiceArgs, serviceConnection)
    } catch (e: Exception) {
        Toast.makeText(this, "启动 Shizuku UserService 失败: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("Shizuku", "启动 UserService 异常", e)
    }
}
```

## 七、常见功能实现

### 1. 执行需要系统权限的命令

```kotlin
// 打开/关闭 WiFi
userService.exec("svc wifi enable")  // 打开 WiFi
userService.exec("svc wifi disable")  // 关闭 WiFi

// 获取 WiFi 状态
val wifiStatus = userService.exec("cmd wifi status")

// 安装/卸载应用
userService.exec("pm install -r /sdcard/app.apk")
userService.exec("pm uninstall com.package.name")

// 授予权限
userService.exec("pm grant com.package.name android.permission.PERMISSION_NAME")

// 启动设置页面
userService.exec("am start -a android.settings.WIFI_SETTINGS")
```

### 2. 持久化服务连接

为了避免每次使用 UserService 都要重新绑定，可以创建一个全局的服务连接管理器：

```kotlin
object ShizukuServiceManager {
    private var userService: IUserService? = null
    private var serviceConnection: ServiceConnection? = null
    
    // 连接状态监听器接口
    interface ConnectionListener {
        fun onServiceConnected()
        fun onServiceDisconnected()
    }
    
    private val connectionListeners = mutableListOf<ConnectionListener>()
    
    // 添加监听器
    fun addConnectionListener(listener: ConnectionListener) {
        connectionListeners.add(listener)
    }
    
    // 移除监听器
    fun removeConnectionListener(listener: ConnectionListener) {
        connectionListeners.remove(listener)
    }
    
    // 检查是否已连接
    fun isConnected(): Boolean = userService != null && userService?.asBinder()?.isBinderAlive == true
    
    // 绑定服务
    fun bindService(context: Context) {
        if (isConnected()) return
        
        try {
            val userServiceArgs = UserServiceArgs(
                ComponentName(
                    context.packageName,
                    UserService::class.java.name
                )
            )
                .daemon(false)
                .processNameSuffix("service")
                .debuggable(BuildConfig.DEBUG)
                .version(BuildConfig.VERSION_CODE)
            
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    userService = IUserService.Stub.asInterface(service)
                    connectionListeners.forEach { it.onServiceConnected() }
                }
                
                override fun onServiceDisconnected(name: ComponentName) {
                    userService = null
                    connectionListeners.forEach { it.onServiceDisconnected() }
                }
            }
            
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        } catch (e: Exception) {
            Log.e("ShizukuServiceManager", "绑定服务失败", e)
        }
    }
    
    // 解绑服务
    fun unbindService() {
        serviceConnection?.let { Shizuku.unbindUserService(it, true) }
        serviceConnection = null
        userService = null
    }
    
    // 执行命令
    fun exec(command: String): String? {
        return try {
            userService?.exec(command)
        } catch (e: Exception) {
            Log.e("ShizukuServiceManager", "执行命令失败", e)
            null
        }
    }
}
```

## 八、最佳实践

1. **检查 Shizuku 可用性**
   - 在进行任何 Shizuku 相关操作前，应当检查 Shizuku 服务是否在运行
   - 如果 Shizuku 服务不可用，应当引导用户安装并激活 Shizuku

2. **权限处理**
   - 在每次启动应用时检查 Shizuku 权限状态
   - 处理用户拒绝权限的情况，提供清晰的引导

3. **异常处理**
   - Shizuku 服务可能会因为各种原因断开连接，需要妥善处理断连情况
   - 捕获并记录 RemoteException 等异常

4. **用户体验**
   - 提供明确的提示，告知用户当前 Shizuku 连接状态
   - 在执行敏感操作之前，向用户解释操作的后果

5. **版本适配**
   - 针对不同版本的 Shizuku 做适配，特别是处理不同的权限机制

## 九、测试与调试

1. **模拟不同的 Shizuku 状态**
   - Shizuku 未安装
   - Shizuku 已安装但未运行
   - Shizuku 已运行但未授权
   - Shizuku 已运行且已授权

2. **权限测试**
   - 测试拒绝权限后的应用行为
   - 测试取消权限后的应用行为

3. **异常情况测试**
   - 测试 Shizuku 服务突然停止的情况
   - 测试执行无效命令的情况

## 十、常见问题

1. **Shizuku 权限被拒绝**
   - 引导用户重新授权
   - 解释为什么应用需要 Shizuku 权限

2. **UserService 无法连接**
   - 检查 Shizuku 服务是否运行
   - 检查 AIDL 接口是否正确实现

3. **命令执行失败**
   - 确认命令语法是否正确
   - 检查是否有足够的权限执行该命令

4. **兼容性问题**
   - 针对不同 Android 版本的命令差异进行适配
   - 处理不同厂商 ROM 的特殊情况

## 十一、安全注意事项

1. **谨慎执行命令**
   - 只执行必要的命令，避免对系统造成损害
   - 限制命令执行范围，不要执行来自不可信来源的命令

2. **保护用户数据**
   - 不要使用 Shizuku 权限访问与应用无关的用户数据
   - 不要滥用 Shizuku 权限执行不必要的操作

3. **避免权限滥用**
   - 明确告知用户应用将执行哪些系统操作
   - 为敏感操作提供额外的确认步骤

## 十二、总结

通过以上步骤，您可以成功接入 Shizuku 服务，实现需要系统权限的功能。请记住，Shizuku 提供的权限非常强大，应当合理使用并确保不会对用户设备造成损害。

在开发过程中，请始终关注 Shizuku 的官方更新和文档，以便适配新版本和特性。