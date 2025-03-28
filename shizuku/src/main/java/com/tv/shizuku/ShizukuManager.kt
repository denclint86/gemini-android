package com.tv.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.zephyr.global_values.TAG
import com.zephyr.global_values.globalContext
import com.zephyr.log.logD
import com.zephyr.log.logE
import com.zephyr.log.toLogString
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs

/**
 * Shizuku 服务管理器
 * 负责 Shizuku 服务的连接、权限管理和命令执行
 */
object ShizukuManager {
    private const val REQUEST_CODE = 100

    private var userService: IUserService? = null
    private var serviceConnection: ServiceConnection? = null
    private var userServiceArgs: UserServiceArgs? = null // 保存 UserServiceArgs 用于解绑

    // 连接状态监听器接口
    interface ConnectionListener {
        fun onServiceConnected()
        fun onServiceDisconnected()
    }

    // 权限状态监听器接口
    interface PermissionListener {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

    private val connectionListeners = mutableListOf<ConnectionListener>()
    private var permissionListener: PermissionListener? = null

    // Shizuku 权限请求结果监听器
    private val shizukuPermissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    logD(TAG, "Shizuku 权限已授予")
                    permissionListener?.onPermissionGranted()
                } else {
                    logD(TAG, "Shizuku 权限被拒绝")
                    permissionListener?.onPermissionDenied()
                }
            }
        }

    /**
     * 初始化 Shizuku 管理器
     * 应在 Application 或主 Activity 的 onCreate 中调用
     */
    fun init() {
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        logD(TAG, "ShizukuManager 初始化完成")
    }

    /**
     * 释放资源
     * 应在 Application 或主 Activity 的 onDestroy 中调用
     */
    fun release() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        unbindService()
        logD(TAG, "ShizukuManager 资源已释放")
    }

    /**
     * 检查 Shizuku 是否运行
     */
    fun isShizukuRunning(): Boolean {
        return try {
            Shizuku.pingBinder().also { result ->
                logD(TAG, "Shizuku 运行状态: $result")
            }
        } catch (e: Exception) {
            logE(TAG, "检查 Shizuku 运行状态失败\n${e.toLogString()}")
            false
        }
    }

    /**
     * 检查是否已授予 Shizuku 权限
     */
    fun hasPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                .also { result -> logD(TAG, "Shizuku 权限状态: $result") }
        } catch (e: Exception) {
            logE(TAG, "检查权限失败\n${e.toLogString()}")
            false
        }
    }

    /**
     * 请求 Shizuku 权限
     * @param listener 权限请求结果监听器
     */
    fun requestPermission(listener: PermissionListener?) {
        permissionListener = listener

        if (hasPermission()) {
            listener?.onPermissionGranted()
            return
        }

        if (!isShizukuRunning()) {
            logE(TAG, "Shizuku 未运行，无法请求权限")
            listener?.onPermissionDenied()
            return
        }

        try {
            if (Shizuku.isPreV11()) {
                logE(TAG, "Shizuku 版本过低，不支持权限请求")
                listener?.onPermissionDenied()
                return
            }

            Shizuku.requestPermission(REQUEST_CODE)
            logD(TAG, "已发起 Shizuku 权限请求")
        } catch (e: Exception) {
            logE(TAG, "请求 Shizuku 权限失败\n${e.toLogString()}")
            listener?.onPermissionDenied()
        }
    }

    /**
     * 添加服务连接监听器
     */
    fun addConnectionListener(listener: ConnectionListener) {
        connectionListeners.add(listener)
        logD(TAG, "添加连接监听器: $listener")
    }

    /**
     * 移除服务连接监听器
     */
    fun removeConnectionListener(listener: ConnectionListener) {
        connectionListeners.remove(listener)
        logD(TAG, "移除连接监听器: $listener")
    }

    /**
     * 检查服务是否已连接
     */
    fun isConnected(): Boolean {
        return userService != null
            .also { result -> logD(TAG, "服务连接状态: $result") }
    }

    /**
     * 绑定 UserService
     */
    fun bindService() {
        if (isConnected()) {
            logD(TAG, "服务已连接，无需重复绑定")
            return
        }

        if (!hasPermission()) {
            logE(TAG, "没有 Shizuku 权限，无法绑定服务")
            return
        }

        try {
            userServiceArgs = UserServiceArgs(
                ComponentName(
                    globalContext!!,
                    UserService::class.java.name
                )
            )
                .daemon(false)
                .processNameSuffix("service")
                .debuggable(BuildConfig.DEBUG)

            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    userService = IUserService.Stub.asInterface(service)
                    logD(TAG, "Shizuku 服务连接成功")
                    connectionListeners.forEach { it.onServiceConnected() }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    userService = null
                    logD(TAG, "Shizuku 服务连接断开")
                    connectionListeners.forEach { it.onServiceDisconnected() }
                }
            }

            serviceConnection?.let {
                Shizuku.bindUserService(userServiceArgs!!, it)
                logD(TAG, "正在绑定 Shizuku 服务")
            }
        } catch (e: Exception) {
            logE(TAG, "绑定 Shizuku 服务失败\n${e.toLogString()}")
        }
    }

    /**
     * 解绑 UserService
     */
    fun unbindService() {
        try {
            serviceConnection?.let { conn ->
                userServiceArgs?.let { args ->
                    Shizuku.unbindUserService(args, conn, true)
                    logD(TAG, "Shizuku 服务已解绑")
                } ?: logE(TAG, "解绑失败: UserServiceArgs 未初始化")
            } ?: logE(TAG, "解绑失败: ServiceConnection 未初始化")
        } catch (e: Exception) {
            logE(TAG, "解绑 Shizuku 服务失败\n${e.toLogString()}")
        } finally {
            // 清理资源
            serviceConnection = null
            userService = null
            userServiceArgs = null
        }
    }

    /**
     * 执行命令
     * @param command 要执行的命令
     */
    fun exec(command: String): Pair<Int?, String?> {
        try {
            if (!isConnected()) {
                logE(TAG, "服务未连接，无法执行命令: $command")
                return Pair(null, null)
            }
            val r = userService?.exec(command).also { result ->
                logD(TAG, "命令执行结果: $command -> $result")
            }
            return Pair(r?.exitCode, r?.output)
        } catch (e: Exception) {
            logE(TAG, "执行命令失败: $command\n${e.toLogString()}")
            return Pair(null, null)
        }
    }
}