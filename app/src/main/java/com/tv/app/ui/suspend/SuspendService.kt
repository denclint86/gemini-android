package com.tv.app.ui.suspend

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import com.tv.app.R
import com.tv.app.alwaysActiveLifecycleOwner
import com.tv.app.createLayoutParam
import com.tv.app.databinding.LayoutSuspendBinding
import com.tv.app.resizeBitmap
import com.tv.app.toBitmap
import com.zephyr.extension.thread.runOnMain
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SuspendService : Service() {
    private val binder = SuspendServiceBinder()

    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null // 悬浮窗View
    private var binding: LayoutSuspendBinding? = null

    private var listener: ItemViewTouchListener.OnTouchEventListener? = null

    private val mediaProjectionManager: MediaProjectionManager? by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private lateinit var metrics: DisplayMetrics
    private var lastX: Int = 0
    private var lastY: Int = 0

    private var isShowing = false

    private val captureMutex = Mutex()

    private fun releaseMediaProjectionParams() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
    }

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            logE(TAG, "MediaProjection stopped")
            releaseMediaProjectionParams()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startForegroundService() // 在服务创建时启动前台服务
        initMetrics()
        initObserve()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        mediaProjection?.let { projection ->
            projection.unregisterCallback(mediaProjectionCallback)
            projection.stop()
        }
        releaseMediaProjectionParams()
        mediaProjection = null  // 在最后置空
        SuspendLiveDataManager.isShowSuspendWindow.removeObservers(alwaysActiveLifecycleOwner)
        SuspendLiveDataManager.suspendText.removeObservers(alwaysActiveLifecycleOwner)
        hideWindow()
        super.onDestroy()
    }

    private fun startForegroundService() {
        val channelId = "SuspendServiceChannel"
        val channelName = "Suspend Service"

        // 创建通知渠道
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        // 创建通知
        val notification: Notification = NotificationCompat.Builder(this@SuspendService, channelId)
            .setContentTitle("Suspend Window")
            .setContentText("Running in background...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        // 启动前台服务
        startForeground(1, notification)
    }

    private fun initMetrics() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
    }

    private fun initObserve() {
        SuspendLiveDataManager.isShowSuspendWindow.observe(alwaysActiveLifecycleOwner) { isShow ->
            logE(TAG, "obser $isShow")
            if (isShow) {
                showWindow()
            } else {
                hideWindow()
            }
        }
    }

    private fun hideWindow() {
        if (!isShowing) return
        floatRootView?.windowToken?.let {
            runCatching {
                val lp = floatRootView?.layoutParams as? WindowManager.LayoutParams
                lp?.let {
                    lastX = lp.x
                    lastY = lp.y
                }
                windowManager.removeView(floatRootView)
                floatRootView = null
                binding?.unbind()
                binding = null
                isShowing = false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        if (isShowing) return
        val layoutParam = createLayoutParam(lastX, lastY)

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(this@SuspendService),
            R.layout.layout_suspend,
            null,
            false
        )

        binding?.manager = SuspendLiveDataManager
        binding?.lifecycleOwner = alwaysActiveLifecycleOwner

        floatRootView = binding?.root
        floatRootView?.setOnTouchListener(
            ItemViewTouchListener(
                layoutParam,
                windowManager,
                TouchEventListenerImpl()
            )
        )
        windowManager.addView(floatRootView, layoutParam)
        isShowing = true
    }

    private fun setupScreenCapture(resultCode: Int, data: Intent) {
        mediaProjection?.stop() // 清理旧实例
        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
        mediaProjection?.let {
            logE(TAG, "已创建 media projection")
            it.registerCallback(mediaProjectionCallback, null) // 注册回调
            imageReader = ImageReader.newInstance(
                metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2
            )
            virtualDisplay?.release()
            virtualDisplay = it.createVirtualDisplay(
                "ScreenCapture",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface, null, null
            )
        }
    }

    private suspend fun captureScreen(): Bitmap? = withContext(Dispatchers.IO) {
        if (virtualDisplay == null || imageReader == null) return@withContext null

        return@withContext captureMutex.withLock {
            try {
                withContext(Dispatchers.Main) {
                    hideWindow()
                }
                delay(30)

                imageReader?.acquireLatestImage()?.let { img ->
                    img.toBitmap(metrics)?.let { bitmap ->
                        resizeBitmap(bitmap, 0.5F).also { bitmap.recycle() }
                    }.also { img.close() }
                }
            } catch (e: Exception) {
                e.logE(TAG)
                null
            } finally {
                withContext(Dispatchers.Main) {
                    showWindow()
                }
            }
        }
    }

    inner class SuspendServiceBinder : Binder() {
        fun getViewBinding() = binding
        fun setupScreenCapture(resultCode: Int, data: Intent) =
            this@SuspendService.setupScreenCapture(resultCode, data)

        suspend fun captureScreen(): Bitmap? = this@SuspendService.captureScreen()

        fun setProgressBarVisibility(visibility: Int) {
            runOnMain {
                binding?.progressBar?.visibility = visibility
            }
        }

        fun setOnTouchEventListener(l: ItemViewTouchListener.OnTouchEventListener?) {
            listener = l
        }
    }

    inner class TouchEventListenerImpl : ItemViewTouchListener.OnTouchEventListener {
        override fun onClick() {
            listener?.onClick()
        }

        override fun onDrag() {
            listener?.onDrag()
        }

        override fun onLongPress() {
            listener?.onLongPress()
        }
    }
}