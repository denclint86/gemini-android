package com.tv.app.old.accessibility.node

import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toPrettyJson

/**
 * 用于管理界面的可见视图
 */
@Deprecated("貌似是可以每次实时获取的")
object AccessibilityListManager {
    var nodeMap: Map<String, Node>? = null
        private set

    private val listeners = mutableListOf<(Map<String, Node>?) -> Unit>()
    val isAvailable: Boolean
        get() = nodeMap?.isNotEmpty() == true

    init {
        addOnUpdateListener { list ->
            val json = list.toPrettyJson()
            logE(TAG, "\n\n$json\n\n")
        }
    }

    fun update(nodeMap: Map<String, Node>?) {
        if (nodeMap == AccessibilityListManager.nodeMap) return
        AccessibilityListManager.nodeMap = nodeMap
        listeners.forEach { listener -> listener.invoke(nodeMap) }
    }

    fun addOnUpdateListener(listener: (Map<String, Node>?) -> Unit) {
        listeners.add(listener)
    }

    fun removeOnUpdateListener(listener: (Map<String, Node>?) -> Unit) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }
}