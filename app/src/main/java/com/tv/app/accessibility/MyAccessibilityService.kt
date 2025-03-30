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
import java.lang.ref.WeakReference

class MyAccessibilityService : AccessibilityService() {
    companion object {
        const val STRING_MAX_SIZE = 30

        var instance: WeakReference<MyAccessibilityService> = WeakReference(null)
            private set
    }

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
        when (event?.eventType) {
//            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
//
//                // 使用 use() 确保资源正确管理
//                rootInActiveWindow?.let { rootNodeInfo ->
//                    try {
//                        val nodeMap = createNodeMap(rootNodeInfo)
//                        AccessibilityListManager.update(nodeMap)
//                    } catch (e: Exception) {
//                        e.logE(TAG)
//                    } finally {
//                        rootNodeInfo.recycle()
//                    }
//                }
//            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                event.packageName?.toString()?.let { pkg ->
                    ForegroundAppManager.update(pkg, this)
                }
            }

            else -> return
        }
    }

    fun getViewMap() =
        rootInActiveWindow?.run {
            try {
                createNodeMap(this)
            } catch (e: Exception) {
                e.logE(TAG)
                null
            } finally {
                recycle()
            }
        }

    private fun createNodeMap(rootNode: AccessibilityNodeInfo): Map<String, Node> {
        val nodeMap = mutableMapOf<String, Node>()
        var count = 0

        fun traverseNode(node: AccessibilityNodeInfo) {
            val rect = Rect()
            node.getBoundsInScreen(rect)

            if (!node.text.isNullOrEmpty()) {
                var str = node.text.toString()
                if (str.length > STRING_MAX_SIZE)
                    str = str.take(STRING_MAX_SIZE) + "..."

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

    override fun onServiceConnected() {
        logD(TAG, "无障碍服务已连接")
        "无障碍服务已连接".toast()
        instance = WeakReference(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logD(TAG, "无障碍服务已解绑")
        "无障碍服务已解绑".toast()
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        logD(TAG, "无障碍服务中断")
        "无障碍服务中断".toast()
    }

    override fun onDestroy() {
        logD(TAG, "无障碍服务已销毁")
        "无障碍服务已销毁".toast()
//        AccessibilityListManager.clearListeners()
        super.onDestroy()
    }
}