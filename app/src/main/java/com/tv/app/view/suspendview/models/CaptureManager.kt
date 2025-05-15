package com.tv.app.view.suspendview.models

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.view.WindowManager
import com.tv.app.view.suspendview.interfaces.ICaptureManager
import com.tv.utils.resizeBitmap
import com.tv.utils.toBitmap
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 屏幕捕获管理器, 负责截取当前屏幕内容
 * 实现了ICapture接口, 提供屏幕捕获功能
 */
class CaptureManager(private val context: Context) : ICaptureManager {
    private val mediaProjectionManager: MediaProjectionManager? by lazy {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private lateinit var metrics: DisplayMetrics
    private val captureMutex = Mutex()

    /**
     * 判断当前捕获服务是否可用
     */
    override val isAvailable: Boolean
        get() = virtualDisplay != null && imageReader != null

    init {
        initMetrics()
    }

    /**
     * 初始化屏幕度量信息
     */
    private fun initMetrics() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
    }

    /**
     * 释放媒体投影相关资源
     */
    private fun releaseMediaProjectionParams() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
    }

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            logE(TAG, "MediaProjection 停止")
            releaseMediaProjectionParams()
        }
    }

    /**
     * 设置媒体投影服务, 初始化屏幕捕获环境
     */
    override fun setup(resultCode: Int, data: Intent) {
        try {
            mediaProjection?.stop()
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
            mediaProjection?.let {
                logE(TAG, "已创建 media projection")
                it.registerCallback(mediaProjectionCallback, null)
                imageReader = ImageReader.newInstance(
                    metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2
                )
                virtualDisplay?.release()
                virtualDisplay = it.createVirtualDisplay(
                    "Gemini-ScreenCapture",
                    metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface, null, null
                )
            }
        } catch (e: Exception) {
            releaseMediaProjectionParams()
            e.logE(TAG)
        }
    }

    /**
     * 捕获当前屏幕内容, 返回缩放后的位图
     */
    override suspend fun capture(): Bitmap? = withContext(Dispatchers.IO) {
        if (!isAvailable) return@withContext null

        return@withContext captureMutex.withLock {
            try {
                imageReader?.acquireLatestImage()?.let { img ->
                    img.toBitmap(metrics)?.let { bitmap ->
                        resizeBitmap(bitmap, 0.5F).also { bitmap.recycle() }
                    }.also { img.close() }
                }
            } catch (e: Exception) {
                e.logE(TAG)
                null
            }
        }
    }

    /**
     * 释放所有资源, 停止屏幕捕获
     */
    override fun release() {
        mediaProjection?.let {
            it.unregisterCallback(mediaProjectionCallback)
            it.stop()
        }
        releaseMediaProjectionParams()
        mediaProjection = null
    }
}