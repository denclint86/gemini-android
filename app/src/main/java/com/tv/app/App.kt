package com.tv.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.View
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.tv.app.chat.ChatViewModel
import com.tv.app.view.suspendview.SuspendViewService
import com.tv.app.view.suspendview.SuspendViewService.Companion.binder
import com.tv.app.view.suspendview.SuspendViewService.Companion.setBinder
import com.tv.tool.models.ShellExecutorModelImpl
import com.tv.utils.accessibility.Accessibility
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.LogLevel
import com.zephyr.log.Logger
import com.zephyr.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class App : Application() {
    companion object {
        lateinit var app: App

//        fun isAppRunning(): Boolean = binder?.service?.application?.run {
//            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//
//            val runningProcesses = activityManager.runningAppProcesses ?: return false
//            return runningProcesses.any { it.processName == this.packageName }.also {
//                logE(TAG, "进程存活: $it")
//            }
//        } ?: false

        private val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                setBinder((service as? SuspendViewService.SuspendServiceBinder), app)
                "悬浮窗服务连接".toast()
                logE(TAG, "悬浮窗 binder 连接")

                binder?.suspendViewManager?.rootVisibility = View.VISIBLE
                binder?.suspendViewManager?.setOnTouchEventListener(ChatViewModel.suspendViewCallback)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                "悬浮窗服务断开".toast()
                binder?.suspendViewManager?.setOnTouchEventListener(null)
                setBinder(null, app)
                logE(TAG, "悬浮窗 binder 断开")
            }
        }

        fun startSuspendService() = app.apply {
            try {
                runCatching {
                    unbindService(serviceConnection)
                }
                setBinder(null, this)
                val intent = Intent(this, SuspendViewService::class.java)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            } catch (t: Throwable) {
                t.logE(TAG)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        Logger.startLogger(this, LogLevel.VERBOSE)
        app = this

        val model = ShellExecutorModelImpl()

        // 尝试直接用 root 启动无障碍
        ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.IO) {
            val accessibilityServiceClassPath = Accessibility::class.java.name

            model.runShell("pm grant $packageName android.permission.SYSTEM_ALERT_WINDOW")
            model.runShell("pm grant $packageName android.permission.WRITE_SECURE_SETTINGS")
            model.runShell("settings put secure enabled_accessibility_services $packageName/$accessibilityServiceClassPath")
            model.runShell("settings put secure accessibility_enabled 1")
            model.runShell("settings get secure enabled_accessibility_services")
        }
    }
}