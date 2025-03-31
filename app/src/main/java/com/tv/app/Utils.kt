package com.tv.app

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.global_values.globalContext
import com.zephyr.log.logE

val alwaysActiveLifecycleOwner = object : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}

fun String.addReturnChars(maxLength: Int): String {
    if (this.length <= maxLength || maxLength <= 0) return this

    val result = StringBuilder()
    var currentIndex = 0

    while (currentIndex < this.length) {
        // 计算剩余长度
        val remainingLength = this.length - currentIndex
        if (remainingLength <= maxLength) {
            result.append(this.substring(currentIndex))
            break
        }

        // 检查自然换行符
        val nextNewline = this.indexOf('\n', currentIndex)
        if (nextNewline != -1 && nextNewline - currentIndex <= maxLength) {
            result.append(this.substring(currentIndex, nextNewline + 1))
            currentIndex = nextNewline + 1
            continue
        }

        // 没有自然换行符时，找合适的断点
        var endIndex = currentIndex + maxLength
        if (endIndex >= this.length) {
            endIndex = this.length
        } else {
            // 回退到最后一个空格（如果有）
            val chunk = this.substring(currentIndex, endIndex)
            val lastSpace = chunk.lastIndexOf(' ')
            if (lastSpace > maxLength / 2) {
                endIndex = currentIndex + lastSpace
            }
        }

        result.append(this.substring(currentIndex, endIndex))
        if (endIndex < this.length) {
            result.append("\n")
        }
        currentIndex = endIndex
        // 跳过可能的空格
        while (currentIndex < this.length && this[currentIndex] == ' ') {
            currentIndex++
        }
    }

    return result.toString()
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

/**
 * 获取手机所有应用列表，需要在主线程外调用
 * 调用前先检查权限
 *
 * @param context 一般是Fragment或者Activity的context
 * @return 返回Map<应用包名，应用名(一般是Manifest的label)>
 */
fun getAllInstalledApps(
    context: Context,
    isPermission: Boolean
): Map<String, String>? {
    if (isPermission) {
        val packageManager = context.packageManager //获取已安装应用列表
        val apps =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA) //每个ApplicationInfo对象代表一个应用

        val appMap = mutableMapOf<String, String>()

        for (app in apps) {
            val appName = packageManager.getApplicationLabel(app).toString()
            val packageName = app.packageName
            appMap[packageName] = appName
        }

        return appMap
    } else {
        "未获取到权限".toast(true)
        return null
    }
}

/**
 * 获取前台应用信息，返回 Pair<包名, 应用名>
 * 如果失败，返回 null
 * 需要权限PACKAGE_USAGE_STATS
 * @param context 一般是Fragment或者Activity的context
 * @return 返回Pair<应用包名，应用名(一般是Manifest的label)>
 */
private fun getForegroundAppInfo(
    context: Context,
    isPermission: Boolean
): Pair<String, String>? {
    if (isPermission) {
        //获取前台应用的包名
        val packageName = getForegroundPackageName(context) ?: return null

        //通过包名获取应用名称
        val packageManager = context.packageManager
        return try {
            //try-catch是捕获找不到packageName的异常
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            Pair(packageName, appName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.logE(context.TAG)
            // 如果应用不存在（如系统进程），返回包名 + "未知"
            Pair(packageName, "未知")
        }
    } else {
        logE(context.TAG, "未获取权限PACKAGE_USAGE_STATS")
        "未获取权限".toast()
        return null
    }
}

/**
 * 获取前台应用的包名（内部方法）
 *
 */
private fun getForegroundPackageName(context: Context): String? {
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val currentTime = System.currentTimeMillis()
    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        currentTime - 1000 * 60, // 查询最近 60 秒
        currentTime
    )

    var foregroundPackage: String? = null
    var lastTimeUsed = 0L

    stats?.forEach { usageStats ->
        if (usageStats.lastTimeUsed > lastTimeUsed) {
            lastTimeUsed = usageStats.lastTimeUsed
            foregroundPackage = usageStats.packageName
        }
    }
    return foregroundPackage
}

/**
 * 检查权限的开启（用于获取前台应用）
 */
fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

/**
 * 跳转到无障碍服务页
 */
fun gotoAccessibilitySettings(context: Context) = gotoSystemSettings(
    context,
    Settings.ACTION_ACCESSIBILITY_SETTINGS
)

/**
 * 跳转到“使用情况访问权限”页
 * 这是获取前台应用需要的权限
 */
fun gotoUsageStatsSettings(context: Context) = gotoSystemSettings(
    context,
    Settings.ACTION_USAGE_ACCESS_SETTINGS
)

/**
 * 跳转到系统设置页
 * @param context Context
 * @param settingsAction 要跳转的设置页Action，例如：
 *                      - Settings.ACTION_USAGE_ACCESS_SETTINGS（使用情况访问权限）
 *                      - Settings.ACTION_APPLICATION_DETAILS_SETTINGS（应用详情页）
 *                      - Settings.ACTION_ACCESSIBILITY_SETTINGS（无障碍设置）
 * @param packageName 可选参数，用于需要传递包名的设置页（如应用详情页）
 * @return Boolean 是否成功跳转
 */
fun gotoSystemSettings(
    context: Context,
    settingsAction: String,
    packageName: String? = null
): Boolean {
    return try {
        val intent = Intent(settingsAction).apply {
            // 处理需要包名的场景（如应用详情页）
            if (packageName != null) {
                data = Uri.fromParts("package", packageName, null)
            }
            // 添加Flag确保能正常跳转
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // 针对部分国产ROM的特殊处理
            if (Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)) {
                // 小米手机可能需要特殊处理
                putExtra("package_name", packageName ?: context.packageName)
            }
        }

        // 检查是否有Activity能处理这个Intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            // 没有对应的Activity，尝试跳转到通用设置页
            fallbackToGeneralSettings(context)
        }
    } catch (e: ActivityNotFoundException) {
        // 捕获Activity不存在的异常
        Log.e("gotoSystemSettings", "Settings activity not found: ${e.message}")
        fallbackToGeneralSettings(context)
    } catch (e: SecurityException) {
        // 捕获权限异常（某些ROM可能限制）
        Log.e("gotoSystemSettings", "Security exception: ${e.message}")
        fallbackToGeneralSettings(context)
    } catch (e: Exception) {
        // 捕获其他所有异常
        Log.e("gotoSystemSettings", "Unexpected error: ${e.message}")
        false
    }
}

/**
 * 备用方案：跳转到通用设置页
 */
private fun fallbackToGeneralSettings(context: Context): Boolean {
    return try {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        e.logE(context.TAG)
        false
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