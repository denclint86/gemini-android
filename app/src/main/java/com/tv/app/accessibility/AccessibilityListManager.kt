package com.tv.app.accessibility

/**
 * 用于管理界面的可见视图
 */
object AccessibilityListManager {
    var nodeList: List<Node>? = null
        private set

    private val listeners = mutableListOf<(List<Node>) -> Unit>()
    val isAvailable: Boolean
        get() = nodeList?.isNotEmpty() == true

//    init {
//        addOnUpdateListener { list ->
//            val json = list.toPrettyJson()
//            logE(TAG, json)
//        }
//    }

    fun update(nodeTree: List<Node>) {
        nodeList = nodeTree
        listeners.forEach { listener -> listener.invoke(nodeTree) }
    }

    fun addOnUpdateListener(listener: (List<Node>) -> Unit) {
        listeners.add(listener)
    }

    fun removeOnUpdateListener(listener: (List<Node>) -> Unit) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }
}