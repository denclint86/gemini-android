package com.tv.app.func.models

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.google.ai.client.generativeai.type.Schema
import com.tv.app.accessibility.MyAccessibilityService
import com.tv.app.checkUsageStatsPermission

data object ForegroundModel : BaseFuncModel() {
    override val name: String
        get() = "get_foreground_app"

    override val description: String
        get() = "Get information about the currently foreground application (package name and app name). Requires USAGE_STATS permission."

    override val parameters: List<Schema<*>> = listOf(
        Schema.str("default", "Just simply pass in 0.")
    )
    override val requiredParameters: List<String> = listOf("default")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val service = MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()

        // 检查是否有使用统计权限
        val hasPermission = checkUsageStatsPermission(service)
        if (!hasPermission) {
            return defaultMap("error", "USAGE_STATS permission not granted")
        }

        // 获取前台应用信息
        val appInfo = getForegroundAppInfo(service, true) ?:
        return defaultMap("error", "Unable to get foreground app info")

        return mapOf(
            "status" to "success",
            "packageName" to appInfo.first,
            "appName" to appInfo.second
        )
    }

    // 从您的工具类中复制的辅助函数

    /**
     * 检查权限的开启（用于获取前台应用）
     */
    private fun checkUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
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
                // 如果应用不存在（如系统进程），返回包名 + "未知"
                Pair(packageName, "未知")
            }
        } else {
            return null
        }
    }

    /**
     * 获取前台应用的包名（内部方法）
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
}