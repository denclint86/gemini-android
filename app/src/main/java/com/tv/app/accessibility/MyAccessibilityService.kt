package com.tv.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toPrettyJson

class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val nodeTree = mutableListOf<Node>()
        val rootNodeInfo = rootInActiveWindow ?: return

        try {
            with(rootNodeInfo) {
                val rect = Rect()
                getBoundsInScreen(rect)
                nodeTree.add(
                    Node(
                        text?.toString() ?: "null",
                        className.toString(),
                        rect.left,
                        rect.top,
                        traverseNodeTree(this)
                    )
                )
            }
            Log.d(TAG, nodeTree.toPrettyJson())
            // 更新单例数据
            AccessibilityTreeManager.updateNodeTree(nodeTree)
        } catch (e: Exception) {
            e.logE(TAG)
        } finally {
            rootNodeInfo.recycle() // 回收根节点
        }
    }

    private fun traverseNodeTree(node: AccessibilityNodeInfo?): List<Node>? {
        if (node == null) return null
        val childNodeTree = mutableListOf<Node>()

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                val rect = Rect()
                childNode.getBoundsInScreen(rect)
                childNodeTree.add(
                    Node(
                        childNode.text?.toString() ?: "null",
                        childNode.className.toString(),
                        rect.left,
                        rect.top,
                        traverseNodeTree(childNode)
                    )
                )
                childNode.recycle() // 回收子节点
            }
        }
        return childNodeTree
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")
    }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected")
        performGlobalAction(GLOBAL_ACTION_HOME) // 回到桌面
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        AccessibilityTreeManager.clearListeners() // 避免内存泄漏
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }
}
