package com.tv.app.accessibility

/**
 * 用于管理界面的可见视图
 */
object AccessibilityListManager {
    var nodeMap: Map<String, Node>? = null
        private set

    private val listeners = mutableListOf<(Map<String, Node>?) -> Unit>()
    val isAvailable: Boolean
        get() = nodeMap?.isNotEmpty() == true

//    init {
//        addOnUpdateListener { list ->
//            val json = list.toPrettyJson()
//            logE(TAG, json)
//        }
//    }

    fun update(nodeTree: Map<String, Node>?) {
        nodeMap = nodeTree
        listeners.forEach { listener -> listener.invoke(nodeTree) }
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