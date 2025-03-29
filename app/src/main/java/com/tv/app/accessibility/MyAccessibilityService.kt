package com.tv.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.tv.app.getScreenSize
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.logD
import com.zephyr.log.logE

class MyAccessibilityService : AccessibilityService() {
    private var w = 0
    private var h = 0

    init {
        getScreenSize().apply {
            w = first
            h = second
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 对于不相关的事件，提前返回
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        // 使用 use() 确保资源正确管理
        rootInActiveWindow?.let { rootNodeInfo ->
            try {
                val nodeMap = createNodeMap(rootNodeInfo)
                AccessibilityListManager.update(nodeMap)

//                val nodeTree = createNodeTree(rootNodeInfo)
//                AccessibilityTreeManager.updateNodeTree(nodeTree)
            } catch (e: Exception) {
                e.logE(TAG)
            } finally {
                rootNodeInfo.recycle()
            }
        }
    }

    private fun createNodeMap(rootNode: AccessibilityNodeInfo): Map<String, Node> {
        val nodeMap = mutableMapOf<String, Node>()
        var count = 0

        fun traverseNode(node: AccessibilityNodeInfo) {
            val rect = Rect()
            node.getBoundsInScreen(rect)

            if (!node.text.isNullOrEmpty() && !isViewFullyInvisible(rect)) {
                var str = node.text.toString()
                if (str.length > 20)
                    str = str.take(20) + "..."

                nodeMap[count.toString()] = Node(
                    str,
                    node.className?.toString(),
                    rect.toNRect()
                )
                count++
            }

            // 递归遍历子节点
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { childNode ->
                    traverseNode(childNode)
                    childNode.recycle()
                }
            }
        }

        traverseNode(rootNode)
        return nodeMap
    }

    /**
     * 判定一个 view 是否完全不可见，如果是，那么应该过滤
     */
    private fun isViewFullyInvisible(nodeRect: Rect): Boolean {
        // 判断矩形是否完全在屏幕外
        return (nodeRect.right <= 0 ||
                nodeRect.left >= w ||
                nodeRect.bottom <= 0 ||
                nodeRect.top >= h)
    }

//    @Deprecated("")
//    private fun createNodeTree(rootNode: AccessibilityNodeInfo): List<Node> {
//        val nodeTree = mutableListOf<Node>()
//
//        // 使用递归帮助器构建性能更好的树
//        fun traverseNode(node: AccessibilityNodeInfo): Node? {
//            val rect = Rect()
//            node.getBoundsInScreen(rect)
//
//            // 对于空节点提前返回
//            if (node.text.isNullOrEmpty()) return null
//
//            val childNodes = mutableListOf<Node>()
//            for (i in 0 until node.childCount) {
//                node.getChild(i)?.let { childNode ->
//                    traverseNode(childNode)?.let { childNodes.add(it) }
//                    childNode.recycle()
//                }
//            }
//
//            return Node(
//                node.text?.toString()?.take(20),
//                node.className?.toString(),
//                rect.toNRect(),
//                childNodes
//            )
//        }
//
//        traverseNode(rootNode)?.let { nodeTree.add(it) }
//        return nodeTree
//    }

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
        AccessibilityListManager.clearListeners()
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logD(TAG, "无障碍服务已解绑")
        "无障碍服务已解绑".toast()
        return super.onUnbind(intent)
    }
}