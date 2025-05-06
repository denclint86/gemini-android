package com.tv.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.google.android.material.color.DynamicColors
import com.tv.app.viewmodel.chat.windowListener
import com.tv.app.view.ui.suspend.SuspendService
import com.zephyr.global_values.TAG
import com.zephyr.log.LogLevel
import com.zephyr.log.Logger
import com.zephyr.log.logE
import java.lang.ref.WeakReference

class App : Application() {
    companion object {
        var binder: WeakReference<SuspendService.SuspendServiceBinder> = WeakReference(null)
            private set

        var instance: WeakReference<App> = WeakReference(null)
            private set
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        Logger.startLogger(this, LogLevel.VERBOSE)
        instance = WeakReference(this)

//        ProcessLifecycleOwner.get().lifecycleScope.launch {
//            ShellExecutorModel.shellManager.exec("pm grant com.tv.bot android.permission.WRITE_SECURE_SETTINGS")
//            ShellExecutorModel.shellManager.exec("settings put secure enabled_accessibility_services com.tv.bot/com.tv.app.accessibility.MyAccessibilityService")
//            ShellExecutorModel.shellManager.exec("settings put secure accessibility_enabled 1")
//            ShellExecutorModel.shellManager.exec("settings get secure enabled_accessibility_services")
//        }

//        ShellExecutorModel.shellManager.executors.forEach { executor ->
//            ProcessLifecycleOwner.get().lifecycleScope.launch {
//                runCatching {
//                    // 尝试获取 root 和 shizuku 权限
//                    executor.exec("echo test")
//                }
//            }
//        }

//        if (hasOverlayPermission())
//            startSuspendService()
    }

    private fun startSuspendService() {
        runCatching {
            unbindService(serviceConnection)
        }
        binder.clear()
        val intent = Intent(this, SuspendService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = WeakReference(service as? SuspendService.SuspendServiceBinder)
            logE(TAG, "suspend window binder has connected")

            binder.get()?.setOnTouchEventListener(windowListener)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder.get()?.setOnTouchEventListener(null)
            binder.clear()
            logE(TAG, "suspend window binder has disconnected")
        }
    }
}