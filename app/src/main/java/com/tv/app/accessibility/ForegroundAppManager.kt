package com.tv.app.accessibility

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.zephyr.global_values.TAG
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 管理当前前台应用信息（线程安全）
 */
object ForegroundAppManager {

    // 使用Pair存储<包名, 应用名>
    @Volatile private var currentApp: Pair<String, String>? = null

    // 线程安全的监听器列表
    private val listeners = CopyOnWriteArrayList<(Pair<String, String>?) -> Unit>()

    // 是否可用（是否有数据）
    val isAvailable: Boolean
        get() = currentApp != null

    /**
     * 直接获取当前前台应用（可能为null）
     */
    fun getCurrentApp(): Pair<String, String>? = currentApp

    /**
     * 获取当前前台应用（非空版）
     * @throws IllegalStateException 如果无数据
     */
    fun requireCurrentApp(): Pair<String, String> =
        currentApp ?: throw IllegalStateException("No foreground app data")

    /**
     * 更新前台应用信息
     * @param packageName 必须非空
     * @param context 用于解析应用名（可空）
     */
    fun update(packageName: String, context: Context?) {
        // 去重检查
        if (currentApp?.first == packageName) return

        val newValue = try {
            val appName = context?.let {
                it.packageManager.getApplicationLabel(
                    it.packageManager.getApplicationInfo(packageName, 0)
                ).toString()
            } ?: packageName // 降级方案

            packageName to appName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "App not found: $packageName")
            packageName to packageName // 使用包名作为fallback
        }

        synchronized(this) {
            currentApp = newValue
        }
        notifyListeners(newValue)
    }

    /**
     * 清空数据
     */
    fun clear() {
        synchronized(this) {
            currentApp = null
        }
        notifyListeners(null)
    }

    /**
     * 添加监听器（立即回调当前值）
     */
    fun addOnChangeListener(listener: (Pair<String, String>?) -> Unit) {
        listeners.add(listener)
        listener(currentApp) // 初始回调
    }

    /**
     * 移除监听器
     */
    fun removeOnChangeListener(listener: (Pair<String, String>?) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners(newValue: Pair<String, String>?) {
        listeners.forEach { listener ->
            try {
                listener(newValue)
            } catch (e: Exception) {
                Log.e(TAG, "Listener error", e)
            }
        }
    }
}