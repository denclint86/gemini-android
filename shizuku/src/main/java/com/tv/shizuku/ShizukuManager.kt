package com.tv.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.zephyr.global_values.TAG
import com.zephyr.global_values.globalContext
import com.zephyr.log.logD
import com.zephyr.log.logE
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs

object ShizukuManager {
    private const val REQUEST_CODE = 100
    private var userService: IUserService? = null
    private var serviceConnection: ServiceConnection? = null
    private var userServiceArgs: UserServiceArgs? = null
    private val connectionListeners = mutableListOf<ConnectionListener>()
    private var permissionListener: PermissionListener? = null

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

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { code, result ->
            if (code == REQUEST_CODE) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    logD(TAG, "Shizuku permission granted")
                    permissionListener?.onPermissionGranted()
                    bindService() // 权限授予后自动绑定服务
                } else {
                    logE(TAG, "Shizuku permission denied")
                    permissionListener?.onPermissionDenied()
                }
            }
        }

    fun init() {
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
    }

    fun release() {
        unbindService()
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        connectionListeners.clear()
        permissionListener = null
    }

    fun isRunning(): Boolean = Shizuku.pingBinder()
    fun hasPermission(): Boolean =
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    fun isConnected(): Boolean = userService != null

    fun requestPermission(listener: PermissionListener) {
        permissionListener = listener
        if (hasPermission()) {
            listener.onPermissionGranted()
            return
        }
        if (!isRunning()) {
            logE(TAG, "Shizuku is not running")
            listener.onPermissionDenied()
            return
        }
        Shizuku.requestPermission(REQUEST_CODE)
    }

    fun addConnectionListener(listener: ConnectionListener) = connectionListeners.add(listener)
    fun removeConnectionListener(listener: ConnectionListener) =
        connectionListeners.remove(listener)

    fun bindService() {
        if (isConnected() || !hasPermission()) return

        userServiceArgs = UserServiceArgs(
            ComponentName(globalContext!!, UserService::class.java.name)
        ).daemon(false).processNameSuffix("service").debuggable(BuildConfig.DEBUG)

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                userService = IUserService.Stub.asInterface(service)
                logD(TAG, "Shizuku service connected")
                connectionListeners.forEach { it.onServiceConnected() }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                userService = null
                logD(TAG, "Shizuku service disconnected")
                connectionListeners.forEach { it.onServiceDisconnected() }
            }
        }.also { Shizuku.bindUserService(userServiceArgs!!, it) }
    }

    fun unbindService() {
        serviceConnection?.let { conn ->
            userServiceArgs?.let { args ->
                Shizuku.unbindUserService(args, conn, true)
            }
        }
        userService = null
        serviceConnection = null
        userServiceArgs = null
    }

    fun exec(command: String): ExecResult {
        return userService?.exec(command)
            ?: throw IllegalStateException("shizuku service is unconnected")
    }
}