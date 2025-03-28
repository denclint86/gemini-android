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
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val nodeTree = mutableListOf<Node>()
        val rootNodeInfo = rootInActiveWindow ?: return

        try {
            with(rootNodeInfo) {
                val rect = Rect()
                getBoundsInScreen(rect)
                nodeTree.add(
                    Node(
                        text?.toString(),
                        className.toString(),
                        rect.toNRect(),
                        traverseNodeTree(this)
                    )
                )
            }
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
                        childNode.text?.toString(),
                        childNode.className.toString(),
                        rect.toNRect(),
                        traverseNodeTree(childNode)
                    )
                )
                childNode.recycle() // 回收子节点
            }
        }
        return childNodeTree
    }

    override fun onInterrupt() {
        logD(TAG, "onInterrupt")
        "无障碍服务中断".toast()
    }

    override fun onServiceConnected() {
        logD(TAG, "onServiceConnected")
//        performGlobalAction(GLOBAL_ACTION_HOME) // 回到桌面
        "无障碍服务连接".toast()
    }

    override fun onDestroy() {
        logD(TAG, "onDestroy")
        "无障碍服务销毁".toast()
        AccessibilityTreeManager.clearListeners() // 避免内存泄漏
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logD(TAG, "onUnbind")
        "无障碍服务解绑".toast()
        return super.onUnbind(intent)
    }
}
