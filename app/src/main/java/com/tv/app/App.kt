package com.tv.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.tv.app.chat.windowListener
import com.tv.app.ui.suspend.SuspendService
import com.tv.app.ui.suspend.SuspendViewModel
import com.zephyr.global_values.TAG
import com.zephyr.log.LogLevel
import com.zephyr.log.Logger
import com.zephyr.log.logE

class App : Application() {
    private var binder: SuspendService.SuspendServiceBinder? = null

    override fun onCreate() {
        super.onCreate()
        Logger.startLogger(this, LogLevel.VERBOSE)

        runCatching {
            Runtime.getRuntime().exec("su")
        }

        if (hasOverlayPermission())
            startSuspendService()
    }

    fun startSuspendService() {
        val intent = Intent(this, SuspendService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        SuspendViewModel.isShowSuspendWindow.postValue(true)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as SuspendService.SuspendServiceBinder
            logE(TAG, "suspend window binder has connected")
            binder?.setOnTouchEventListener(windowListener)
            binder?.setOnTouchEventListener(windowListener)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder?.setOnTouchEventListener(null)
            binder = null
            logE(TAG, "suspend window binder has disconnected")
        }
    }
}