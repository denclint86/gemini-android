package com.tv.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.logD
import com.zephyr.log.logE

class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 对于不相关的事件，提前返回
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        // 使用 use() 确保资源正确管理
        rootInActiveWindow?.let { rootNodeInfo ->
            try {
                val nodeTree = createNodeTree(rootNodeInfo)
                AccessibilityTreeManager.updateNodeTree(nodeTree)
            } catch (e: Exception) {
                e.logE(TAG)
            } finally {
                rootNodeInfo.recycle()
            }
        }
    }

    // 重构为更高效地创建节点树的方法
    private fun createNodeTree(rootNode: AccessibilityNodeInfo): List<Node> {
        val nodeTree = mutableListOf<Node>()

        // 使用递归帮助器构建性能更好的树
        fun traverseNode(node: AccessibilityNodeInfo): Node? {
            val rect = Rect()
            node.getBoundsInScreen(rect)

            // 对于空节点提前返回
            if (node.text.isNullOrEmpty() && node.className.isNullOrEmpty()) return null

            val childNodes = mutableListOf<Node>()
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { childNode ->
                    traverseNode(childNode)?.let { childNodes.add(it) }
                    childNode.recycle()
                }
            }

            return Node(
                node.text?.toString(),
                node.className?.toString(),
                rect.toNRect(),
                childNodes
            )
        }

        traverseNode(rootNode)?.let { nodeTree.add(it) }
        return nodeTree
    }

    override fun onInterrupt() {
        logD(TAG, "无障碍服务中断")
        "无障碍服务中断".toast()
    }

    override fun onServiceConnected() {
        logD(TAG, "无障碍服务已连接")
        "无障碍服务已连接".toast()
    }

    override fun onDestroy() {
        logD(TAG, "无障碍服务已销毁")
        "无障碍服务已销毁".toast()
        AccessibilityTreeManager.clearListeners()
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logD(TAG, "无障碍服务已解绑")
        "无障碍服务已解绑".toast()
        return super.onUnbind(intent)
    }
}