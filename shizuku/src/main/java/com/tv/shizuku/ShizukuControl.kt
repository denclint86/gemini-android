package com.tv.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.zephyr.global_values.TAG
import com.zephyr.global_values.globalContext
import com.zephyr.log.logD
import com.zephyr.log.logE
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import kotlin.coroutines.resume

/*
            // 1. 检查 Shizuku 是否可用（可选）
            if (!shizukuControl.ping()) {
                Toast.makeText(this@MainActivity, "Shizuku 未运行，请启动", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 2. 请求权限
            val permissionResult = shizukuControl.requestPermission()
            if (!permissionResult.isSuccess) {
                Toast.makeText(this@MainActivity, "权限请求失败: ${permissionResult.msg}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 3. 等待服务启动（可选但推荐）
            val serviceResult = shizukuControl.waitForService()
            if (!serviceResult.isSuccess) {
                Toast.makeText(this@MainActivity, "服务未启动: ${serviceResult.msg}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 4. 绑定服务
            val bindResult = shizukuControl.bindService()
            if (!bindResult.isSuccess) {
                Toast.makeText(this@MainActivity, "服务绑定失败: ${bindResult.msg}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 5. 执行命令
            try {
                val execResult = shizukuControl.exec("whoami")
                Toast.makeText(this@MainActivity, "命令结果: $execResult", Toast.LENGTH_LONG).show()
            } catch (e: IllegalStateException) {
                Toast.makeText(this@MainActivity, "执行失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            // 6. 释放资源（视需求）
            shizukuControl.release()
 */

object ShizukuControl {
    private const val REQUEST_CODE = 100
    private const val DEFAULT_TIMEOUT_MS = 10000L // 默认超时时间 10 秒

    data class Result(
        val isSuccess: Boolean = false,
        val msg: String = ""
    )

    private var userService: IUserService? = null
    private var serviceConnection: ServiceConnection? = null
    private var userServiceArgs: UserServiceArgs? = null
    private var isRunning = false

    // 检查是否已有权限
    private fun hasPermission(): Boolean =
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    // 检查服务是否已连接
    private fun isConnected(): Boolean = userService != null

    // 检查 Shizuku 是否可用
    fun ping(): Boolean = Shizuku.pingBinder()

    // 阻塞方法：等待服务启动
    suspend fun waitForService(timeoutMs: Long = DEFAULT_TIMEOUT_MS): Result =
        withTimeoutOrNull(timeoutMs) {
            try {
                suspendCancellableCoroutine { continuation ->
                    val listener = object : Shizuku.OnBinderReceivedListener {
                        override fun onBinderReceived() {
                            if (!continuation.isCompleted) { // 检查是否已完成
                                logE(TAG, "")
                                continuation.resume(Result(true))
                            }
                            Shizuku.removeBinderReceivedListener(this)
                        }
                    }

                    Shizuku.addBinderReceivedListenerSticky(listener)
                    if (ping()) {
                        if (!continuation.isCompleted) { // 检查是否已完成
                            continuation.resume(Result(true))
                        }
                        Shizuku.removeBinderReceivedListener(listener)
                        return@suspendCancellableCoroutine
                    }

                    continuation.invokeOnCancellation {
                        Shizuku.removeBinderReceivedListener(listener)
                    }
                }
            } catch (t: Throwable) {
                Result(false, t.message ?: "未知错误")
            }
        } ?: Result(false, "等待服务启动超时")

    // 阻塞方法：请求权限
    suspend fun requestPermission(timeoutMs: Long = DEFAULT_TIMEOUT_MS): Result =
        withTimeoutOrNull(timeoutMs) {
            try {
                val waitResult = waitForService(timeoutMs)
                if (!waitResult.isSuccess) {
                    return@withTimeoutOrNull waitResult
                }

                suspendCancellableCoroutine { continuation ->
                    if (hasPermission()) {
                        continuation.resume(Result(true))
                        return@suspendCancellableCoroutine
                    }

                    if (Shizuku.isPreV11()) {
                        continuation.resume(Result(false, "不支持动态申请"))
                        return@suspendCancellableCoroutine
                    }

                    val listener = object : Shizuku.OnRequestPermissionResultListener {
                        override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                            val granted = grantResult == PackageManager.PERMISSION_GRANTED
                            val msg = if (granted) "" else "请求权限被拒绝"
                            if (!continuation.isCompleted) { // 检查是否已完成
                                continuation.resume(Result(granted, msg))
                            }
                            Shizuku.removeRequestPermissionResultListener(this)
                        }
                    }

                    Shizuku.addRequestPermissionResultListener(listener)
                    Shizuku.requestPermission(REQUEST_CODE)

                    continuation.invokeOnCancellation {
                        Shizuku.removeRequestPermissionResultListener(listener)
                    }
                }
            } catch (t: Throwable) {
                Result(false, t.message ?: "未知错误")
            }
        } ?: Result(false, "请求权限超时")

    // 阻塞方法：绑定服务
    suspend fun bindService(timeoutMs: Long = DEFAULT_TIMEOUT_MS): Result =
        withTimeoutOrNull(timeoutMs) {
            try {
                val permissionResult = requestPermission(timeoutMs)
                if (!permissionResult.isSuccess) {
                    return@withTimeoutOrNull permissionResult
                }

                if (isConnected()) {
                    return@withTimeoutOrNull Result(true)
                }

                suspendCancellableCoroutine { continuation ->
                    userServiceArgs = UserServiceArgs(
                        ComponentName(globalContext!!, UserService::class.java.name)
                    ).daemon(true)
                        .processNameSuffix("tv.bot.shizuku.service")
                        .debuggable(false)

                    serviceConnection = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName, service: IBinder) {
                            userService = IUserService.Stub.asInterface(service)
                            isRunning = true
                            logD(TAG, "Shizuku service connected")
                            if (!continuation.isCompleted) { // 检查是否已完成
                                continuation.resume(Result(true))
                            }
                        }

                        override fun onServiceDisconnected(name: ComponentName) {
                            userService = null
                            isRunning = false
                            logD(TAG, "Shizuku service disconnected")
                        }
                    }

                    Shizuku.bindUserService(
                        userServiceArgs!!,
                        serviceConnection as ServiceConnection
                    )

                    continuation.invokeOnCancellation {
                        unbindService()
                    }
                }
            } catch (t: Throwable) {
                Result(false, t.message ?: "绑定服务失败")
            }
        } ?: Result(false, "绑定服务超时")

    // 阻塞方法：执行命令
    suspend fun exec(command: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS): ExecResult =
        withTimeoutOrNull(timeoutMs) {
            val bindResult = bindService(timeoutMs)
            if (!bindResult.isSuccess) {
                throw IllegalStateException(bindResult.msg)
            }

            return@withTimeoutOrNull userService?.exec(command)
        } ?: throw IllegalStateException("Shizuku request timeout")

    // 释放资源
    fun release() {
        unbindService()
    }

    // 解绑服务
    private fun unbindService() {
        serviceConnection?.let { conn ->
            userServiceArgs?.let { args ->
                Shizuku.unbindUserService(args, conn, true)
            }
        }
        userService = null
        serviceConnection = null
        userServiceArgs = null
        isRunning = false
    }

    // 阻塞方法：等待服务终止
//    suspend fun waitForServiceDead(timeoutMs: Long = DEFAULT_TIMEOUT_MS): Result =
//        withTimeoutOrNull(timeoutMs) {
//            try {
//                suspendCancellableCoroutine { continuation ->
//                    val listener = object : Shizuku.OnBinderDeadListener {
//                        override fun onBinderDead() {
//                            continuation.resume(Result(true))
//                            Shizuku.removeBinderDeadListener(this)
//                        }
//                    }
//
//                    Shizuku.addBinderDeadListener(listener)
//                    if (!Shizuku.pingBinder()) {
//                        continuation.resume(Result(true))
//                    }
//
//                    continuation.invokeOnCancellation {
//                        Shizuku.removeBinderDeadListener(listener)
//                    }
//                }
//            } catch (t: Throwable) {
//                Result(false, t.message ?: "未知错误")
//            }
//        } ?: Result(false, "等待服务终止超时")
}
