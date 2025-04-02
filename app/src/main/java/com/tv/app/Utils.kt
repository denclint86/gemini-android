package com.tv.app

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.tv.app.func.models.ShellExecutorModel
import com.zephyr.global_values.globalContext
import com.zephyr.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
 * 获取手机所有已安装应用列表，需要在主线程外调用
 *
 * @return Set<Array<String>>，如果获取失败返回空 Set （这样字少一点）
 */
// 权限问题
//fun getAllInstalledApps(): Set<Array<String>> {
//    return try {
//        val packageManager = globalContext!!.packageManager
//        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
//        val appMap = mutableSetOf<Array<String>>()
//
//        for (app in apps) {
//            val appName = packageManager.getApplicationLabel(app).toString()
//            val packageName = app.packageName
//            appMap.add(arrayOf(appName, packageName))
//        }
//        appMap
//    } catch (e: Exception) {
//        logE("getAllInstalledApps", e.toString())
//        setOf()
//    }
//}


enum class AppType(val v: String) {
    ALL("all"),        // 所有应用
    SYSTEM("system"),     // 系统应用
    USER("user")        // 用户安装的应用
}

// 获取应用列表的函数
suspend fun getAllInstalledApps(appType: AppType): Set<Array<String>> {
    return withContext(Dispatchers.IO) {
        val packageManager = globalContext?.packageManager ?: return@withContext setOf()
        val appSet = mutableSetOf<Array<String>>()

        // 使用 ShellExecutorModel 执行 pm list packages 命令
        val command = "pm list packages"
        val result = ShellExecutorModel.call(
            mapOf(
                "command" to command,
                "timeout" to 5000L
            )
        )

        val output = result["output"] as? String

        if (output.isNullOrEmpty()) {
            logE("getAllInstalledApps", "Shell command failed: $result")
            return@withContext setOf()
        }

        // 解析 shell 输出，提取包名
        val packageNames = output.lines()
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:") }

        // 通过 PackageManager 获取应用信息并筛选
        for (packageName in packageNames) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()

                // 根据 appType 筛选
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                when (appType) {
                    AppType.ALL -> appSet.add(arrayOf(appName, packageName))
                    AppType.SYSTEM -> if (isSystemApp) appSet.add(arrayOf(appName, packageName))
                    AppType.USER -> if (!isSystemApp) appSet.add(arrayOf(appName, packageName))
                }
            } catch (e: PackageManager.NameNotFoundException) {
                logE("getAllInstalledApps", "App not found: $packageName")
            }
        }
        appSet
    }
}

/**
 * 跳转到无障碍服务设置页面
 *
 * @param context Context
 * @return 是否成功跳转
 */
fun gotoAccessibilitySettings(context: Context): Boolean {
    return gotoSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS)
}

/**
 * 跳转到“使用情况访问权限”设置页面（可选保留，用于其他功能）
 *
 * @param context Context
 * @return 是否成功跳转
 */
fun gotoUsageStatsSettings(context: Context): Boolean {
    return gotoSystemSettings(context, Settings.ACTION_USAGE_ACCESS_SETTINGS)
}

/**
 * 跳转到指定系统设置页面
 *
 * @param context Context
 * @param settingsAction 设置页面的 Action，例如：
 *   - Settings.ACTION_ACCESSIBILITY_SETTINGS（无障碍设置）
 *   - Settings.ACTION_USAGE_ACCESS_SETTINGS（使用情况访问权限）
 *   - Settings.ACTION_APPLICATION_DETAILS_SETTINGS（应用详情）
 * @param packageName 可选，用于需要包名的设置页面（如应用详情）
 * @return 是否成功跳转
 */
fun gotoSystemSettings(
    context: Context,
    settingsAction: String,
    packageName: String? = null
): Boolean {
    return try {
        val intent = Intent(settingsAction).apply {
            if (packageName != null) {
                data = Uri.fromParts("package", packageName, null)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // 小米设备特殊处理
            if (Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)) {
                putExtra("package_name", packageName ?: context.packageName)
            }
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            fallbackToGeneralSettings(context)
        }
    } catch (e: Exception) {
        logE("gotoSystemSettings", e.toString())
        fallbackToGeneralSettings(context)
    }
}

/**
 * 备用方案：跳转到通用设置页面
 *
 * @param context Context
 * @return 是否成功跳转
 */
private fun fallbackToGeneralSettings(context: Context): Boolean {
    return try {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        logE("fallbackToGeneralSettings", e.toString())
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