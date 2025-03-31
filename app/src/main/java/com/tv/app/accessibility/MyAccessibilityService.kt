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
    private lateinit var nodeTracker: NodeTracker

    init {
        getScreenSize().apply {
            w = first
            h = second
        }
    }

    override fun onServiceConnected() {
        logD(TAG, "无障碍服务已连接")
        "无障碍服务已连接".toast()
        instance = WeakReference(this)
        nodeTracker = NodeTracker(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                event.packageName?.toString()?.let { pkg ->
                    ForegroundAppManager.update(pkg, this)
                }
            }

            else -> return
        }
    }

    fun getViewMap(): Map<String, Node>? {
        return rootInActiveWindow?.run {
            try {
                createNodeMap(this)
            } catch (e: Exception) {
                e.logE(TAG)
                null
            } finally {
                recycle()
            }
        }
    }

    private fun createNodeMap(rootNode: AccessibilityNodeInfo): Map<String, Node> {
        val nodeMap = mutableMapOf<String, Node>()
        traverseNode(rootNode) { node ->
            val rect = Rect()
            node.getBoundsInScreen(rect)

            if (!node.text.isNullOrEmpty()) {
                val hash = nodeTracker.generateNodeHash(node)
                var str = node.text.toString()
                if (str.length > STRING_MAX_SIZE)
                    str = str.take(STRING_MAX_SIZE) + "..."
                nodeMap[hash] = Node(
                    str,
                    node.className?.toString(),
                    if (node.isEditable) true else null,
                    if (node.isAccessibilityFocused) true else null,
                    rect.toNRect()
                )
            }
        }
        return nodeMap
    }

    suspend fun getNodeByHash(hash: String): AccessibilityNodeInfo? {
        return nodeTracker.findNodeByHash(hash)
    }

    fun traverseNode(
        node: AccessibilityNodeInfo?,
        shouldRecycle: Boolean = true,
        action: (AccessibilityNodeInfo) -> Unit
    ) {
        if (node == null) return
        try {
            action(node)
        } catch (t: Throwable) {
            t.logE(TAG)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { childNode ->
                traverseNode(childNode, shouldRecycle, action)
                if (shouldRecycle)
                    childNode.recycle()
            }
        }
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
        super.onDestroy()
    }
}