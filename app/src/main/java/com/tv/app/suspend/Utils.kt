package com.tv.app.suspend

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.logE

object Utils {
    const val REQUEST_FLOAT_CODE = 1001

    fun isNull(any: Any?): Boolean = any == null

    fun commonROMPermissionCheck(context: Context?): Boolean {
        var result = true
        try {
            val clazz: Class<*> = Settings::class.java
            val canDrawOverlays =
                clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
            result = canDrawOverlays.invoke(null, context) as Boolean
        } catch (e: Exception) {
            e.logE(TAG)
        }
        return result
    }

    fun checkSuspendedWindowPermission(context: Activity, block: () -> Unit) {
        if (commonROMPermissionCheck(context)) {
            block()
        } else {
            "请开启悬浮窗权限".toast()
            context.startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }, REQUEST_FLOAT_CODE)
        }
    }
}