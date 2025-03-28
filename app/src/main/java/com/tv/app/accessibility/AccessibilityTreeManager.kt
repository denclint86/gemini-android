package com.tv.app.accessibility

/**
 * 用于管理界面视图树
 * 注意更新UI要切换回主线程
 */
object AccessibilityTreeManager {
    private var _nodeTree: List<Node>? = null
    private val listeners = mutableListOf<(List<Node>) -> Unit>()

    // 更新节点树
    fun updateNodeTree(nodeTree: List<Node>) {
        _nodeTree = nodeTree
        listeners.forEach { it(nodeTree) }
    }

    // 获取最新的节点树（可能为null）
    fun getNodeTree(): List<Node>? = _nodeTree

    // 注册监听器，当节点树更新时回调
    fun addNodeTreeListener(listener: (List<Node>) -> Unit) {
        listeners.add(listener)
    }

    // 移除监听器
    fun removeNodeTreeListener(listener: (List<Node>) -> Unit) {
        listeners.remove(listener)
    }

    // 清空监听器（防止内存泄漏）
    fun clearListeners() {
        listeners.clear()
    }
}