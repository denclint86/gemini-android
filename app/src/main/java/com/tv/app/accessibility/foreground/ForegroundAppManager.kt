package com.tv.app.accessibility.foreground

import com.zephyr.global_values.TAG
import com.zephyr.global_values.globalContext
import com.zephyr.log.logE

/**
 * 管理当前前台应用信息
 */
data object ForegroundAppManager {

    // 使用Pair存储<包名, 应用名>，保证Pair本身非空
    @Volatile
    private var currentApp: Pair<String?, String?> = Pair(null, null) // 初始值非空

    // 线程安全的监听器列表
    private val listeners = mutableListOf<(Pair<String?, String?>) -> Unit>()

    // 是否可用（是否有有效数据）
    val isAvailable: Boolean
        get() = currentApp.first != null // 判断包名是否非空作为可用性依据

    /**
     * 直接获取当前前台应用（始终返回非空Pair）
     */
    fun getCurrentApp(): Pair<String?, String?> = currentApp

    /**
     * 更新前台应用信息
     * @param packageName 包名，可为空
     */
    fun update(packageName: String?) {
        if (currentApp.first == packageName) return

        val newValue = if (packageName == null) {
            Pair(null, null) // packageName为空时，重置为(null, null)
        } else {
            try {
                val appName = globalContext?.let {
                    it.packageManager.getApplicationLabel(
                        it.packageManager.getApplicationInfo(packageName, 0)
                    ).toString()
                } ?: packageName // 降级方案

                packageName to appName
            } catch (e: Exception) {
                e.logE(TAG)
                packageName to packageName // 使用包名作为fallback
            }
        }

        synchronized(this) {
            currentApp = newValue
        }
        notifyListeners(newValue)
    }

    /**
     * 清空数据（重置为初始状态）
     */
    fun clear() {
        synchronized(this) {
            currentApp = Pair(null, null) // 重置为非空的Pair(null, null)
        }
        notifyListeners(Pair(null, null))
    }

    /**
     * 添加监听器（立即回调当前值）
     */
    fun addOnChangeListener(listener: (Pair<String?, String?>) -> Unit) {
        synchronized(listeners) {
            listeners.add(listener)
        }
        listener(currentApp) // 初始回调
    }

    /**
     * 移除监听器
     */
    fun removeOnChangeListener(listener: (Pair<String?, String?>) -> Unit) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    private fun notifyListeners(newValue: Pair<String?, String?>) {
        synchronized(listeners) {
            listeners.forEach { listener ->
                try {
                    listener(newValue)
                } catch (e: Exception) {
                    e.logE(TAG)
                }
            }
        }
    }
}