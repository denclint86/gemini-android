package com.tv.app.view.suspendview

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tv.app.App
import com.tv.app.chat.beans.ChatMessage
import com.tv.app.view.suspendview.interfaces.ICaptureManager
import com.tv.app.view.suspendview.interfaces.ISuspendViewManager
import com.tv.app.view.suspendview.models.CaptureManager
import com.tv.app.view.suspendview.models.SuspendViewManager
import com.tv.utils.Role
import com.zephyr.extension.thread.runOnMain
import com.zephyr.global_values.TAG
import com.zephyr.log.logE

class SuspendViewService : Service() {
    companion object {
        val alwaysActiveLifecycleOwner = object : LifecycleOwner {
            private val lifecycleRegistry = LifecycleRegistry(this).apply {
                currentState = Lifecycle.State.RESUMED
            }

            override val lifecycle: Lifecycle
                get() = lifecycleRegistry
        }

        var binder: SuspendServiceBinder? = null
            private set

//        private val _suspendViewVisibility = MutableLiveData(View.VISIBLE)
//        val suspendViewVisibility: LiveData<Int>
//            get() = _suspendViewVisibility

        private val _suspendText = MutableLiveData("Hi")
        val suspendText: LiveData<String>
            get() = _suspendText

        fun setBinder(binder: SuspendServiceBinder?, app: App) {
            Companion.binder = binder
        }

        fun update(last: ChatMessage) {
            val v = when {
                last.role == Role.SYSTEM -> "未开始聊天"
                last.role == Role.MODEL && last.text.isNotBlank() -> {
                    binder?.suspendViewManager?.progressBarVisibility = View.INVISIBLE
                    last.text.take(4) + "..."
                }

                else -> {
                    binder?.suspendViewManager?.progressBarVisibility = View.VISIBLE
                    "正在生成"
                }
            }

            setSuspendText(v)
        }

        fun setSuspendText(text: String) = runOnMain {
            _suspendText.value = text
        }
    }

    private val captureManager: ICaptureManager by lazy {
        CaptureManager(this)
    }

    private val suspendViewManager: ISuspendViewManager by lazy {
        SuspendViewManager(this)
    }

    private val binder = SuspendServiceBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        try {
            startForegroundService()
//            initObserve()
        } catch (e: Exception) {
            e.logE(TAG)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        captureManager.release()
        suspendViewManager.release()
//        suspendViewVisibility.removeObservers(alwaysActiveLifecycleOwner)
        super.onDestroy()
    }

    private fun startForegroundService() {
        val channelId = "gemini-suspend-view-service"
        val channelName = "Gemini 悬浮窗前台服务"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("悬浮窗前台服务")
            .setContentText("运行中")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            startForeground(1, notification)
            notificationManager.createNotificationChannel(channel)
        } else {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        }
    }

//    private fun initObserve() {
//        suspendViewVisibility.observe(alwaysActiveLifecycleOwner) { int ->
//            suspendViewManager.rootVisibility = (int ?: View.VISIBLE)
//        }
//    }

    inner class SuspendServiceBinder : Binder() {
        val captureManager: ICaptureManager
            get() = this@SuspendViewService.captureManager
        val suspendViewManager: ISuspendViewManager
            get() = this@SuspendViewService.suspendViewManager
    }
}