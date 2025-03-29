package com.tv.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.zephyr.global_values.globalContext

val alwaysActiveLifecycleOwner = object : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}

fun hasOverlayPermission(): Boolean =
    Settings.canDrawOverlays(globalContext)

fun resizeBitmap(bitmap: Bitmap, scaleFactor: Float): Bitmap {
    val newWidth = (bitmap.width * scaleFactor).toInt()
    val newHeight = (bitmap.height * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun createLayoutParam(x: Int = 0, y: Int = 0) = WindowManager.LayoutParams().apply {
    type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    format = PixelFormat.RGBA_8888
    flags =
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    width = WindowManager.LayoutParams.WRAP_CONTENT
    height = WindowManager.LayoutParams.WRAP_CONTENT
    gravity = Gravity.START or Gravity.TOP

    this.x = x
    this.y = y
}

fun Image.toBitmap(metrics: DisplayMetrics): Bitmap? {
    val planes = planes.takeIf { it.isNotEmpty() } ?: return null
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * metrics.widthPixels

    return Bitmap.createBitmap(
        metrics.widthPixels + rowPadding / pixelStride,
        metrics.heightPixels,
        Bitmap.Config.ARGB_8888
    ).apply {
        copyPixelsFromBuffer(buffer)
    }.let {
        Bitmap.createBitmap(it, 0, 0, metrics.widthPixels, metrics.heightPixels)
    }
}

/**
 * 屏幕信息，pair<宽，高>
 */
fun getScreenSize(): Pair<Int, Int> {
    val windowManager = globalContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.currentWindowMetrics.bounds
        Pair(bounds.width(), bounds.height())
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}

//
//// 测试用
//fun saveBitmapToExternalStorage(
//    context: Context,
//    bitmap: Bitmap,
//    filename: String,
//    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
//    quality: Int = 100
//): String? {
//    return try {
//        // 获取应用的外部存储目录（Android/data/包名/files/）
//        val directory = context.getExternalFilesDir(null)
//        if (directory != null && !directory.exists()) {
//            directory.mkdirs()
//        }
//
//        val file = File(directory, filename)
//        val outputStream = FileOutputStream(file)
//
//        // 将bitmap压缩并写入到文件
//        bitmap.compress(format, quality, outputStream)
//
//        // 关闭输出流
//        outputStream.flush()
//        outputStream.close()
//
//        // 返回保存的文件路径
//        file.absolutePath
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}
//
//// 测试用
//fun saveBitmapToSubdirectory(
//    context: Context,
//    bitmap: Bitmap,
//    subdirectory: String,
//    filename: String,
//    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
//    quality: Int = 100
//): String? {
//    return try {
//        // 获取应用的外部存储目录，并添加子目录
//        val parentDir = context.getExternalFilesDir(null)
//        val directory = File(parentDir, subdirectory)
//
//        // 确保目录存在
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//
//        val file = File(directory, filename)
//        val outputStream = FileOutputStream(file)
//
//        // 将bitmap压缩并写入到文件
//        bitmap.compress(format, quality, outputStream)
//
//        // 关闭输出流
//        outputStream.flush()
//        outputStream.close()
//
//        // 返回保存的文件路径
//        file.absolutePath
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}