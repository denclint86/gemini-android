package com.tv.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.zephyr.global_values.TAG
import com.zephyr.global_values.globalContext
import com.zephyr.log.logE

/**
 * 同步操作的计时函数
 */
suspend fun <T> Any.count(actionName: String = "", block: suspend () -> T): T {
    val s = System.currentTimeMillis()
    val t = block()
    return t.also {
        logE(TAG, "$actionName 耗时 ${System.currentTimeMillis() - s} ms")
    }
}

fun getAppInfo(packageName: String) = packageManager.getApplicationInfo(packageName, 0)

fun getAppName(packageName: String): String =
    packageManager.getApplicationLabel(getAppInfo(packageName)).toString()

val packageManager: PackageManager
    get() = globalContext!!.packageManager

fun hasOverlayPermission(): Boolean =
    Settings.canDrawOverlays(globalContext)

/**
 * 屏幕信息, pair<宽, 高>
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
 * 跳转到无障碍服务设置页面
 *
 * @param context Context
 * @return 是否成功跳转
 */
fun gotoAccessibilitySettings(context: Context): Boolean {
    return gotoSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS)
}

/**
 * 跳转到"使用情况访问权限"设置页面 (可选保留, 用于其他功能)
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
 * @param settingsAction 设置页面的 Action, 例如:
 *   - Settings.ACTION_ACCESSIBILITY_SETTINGS (无障碍设置)
 *   - Settings.ACTION_USAGE_ACCESS_SETTINGS (使用情况访问权限)
 *   - Settings.ACTION_APPLICATION_DETAILS_SETTINGS (应用详情)
 * @param packageName 可选, 用于需要包名的设置页面 (如应用详情)
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
 * 备用方案: 跳转到通用设置页面
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