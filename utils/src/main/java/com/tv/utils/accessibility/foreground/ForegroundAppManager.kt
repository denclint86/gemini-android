package com.tv.utils.accessibility.foreground

import com.tv.utils.getAppName
import com.zephyr.global_values.TAG
import com.zephyr.log.logE

/**
 * 管理当前前台应用信息
 */
class ForegroundAppManager : IForeground {

    @Volatile
    private var _currentApp: Pair<String, String>? = null
        set(value) {
            field = value
            notifyListeners(value)
        }

    override val currentApp: Pair<String, String>?
        get() = _currentApp

    private val callbacks = mutableListOf<IForeground.ChangeCallback>()


    override fun update(packageName: String?) {
        if (currentApp?.first == packageName) return

        val newValue =
            if (packageName == null) {
                null
            } else {
                try {
                    val appName = getAppName(packageName)

                    packageName to appName
                } catch (e: Exception) {
                    e.logE(TAG)
                    packageName to "" // 使用包名作为 fallback
                }
            }

        synchronized(this) {
            _currentApp = newValue
        }
    }

    override fun clear() = synchronized(this) {
        _currentApp = null
    }

    override fun addOnChangeListener(callback: IForeground.ChangeCallback) {
        synchronized(callbacks) {
            callbacks.add(callback)
        }
        callback.onChange(currentApp)
    }

    override fun removeOnChangeListener(callback: IForeground.ChangeCallback) {
        synchronized(callbacks) {
            callbacks.remove(callback)
        }
    }

    private fun notifyListeners(newValue: Pair<String, String>?) {
        synchronized(callbacks) {
            callbacks.forEach { listener ->
                try {
                    listener.onChange(newValue)
                } catch (e: Exception) {
                    e.logE(TAG)
                }
            }
        }
    }
}