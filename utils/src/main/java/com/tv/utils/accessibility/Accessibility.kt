package com.tv.utils.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.tv.utils.accessibility.beans.Node
import com.tv.utils.accessibility.beans.NodeHandler
import com.tv.utils.accessibility.foreground.ForegroundAppManager
import com.tv.utils.accessibility.foreground.IForeground
import com.tv.utils.createNodeMap
import com.tv.utils.scroll
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.logD
import com.zephyr.log.logE

class Accessibility : AccessibilityService(), IAccessibility {

    companion object {
        var instance: IAccessibility? = null
            private set

        /**
         * 记录下来的视图类型
         */
        val SAVE_LIST = setOf(
            "button",
            "textview",
            "edittext",
            "imageview",
            "checkbox",
            "webview",
        )
    }

    private lateinit var nodeHandler: NodeHandler
    private var iForeground: IForeground = ForegroundAppManager()

    override val viewMap: Map<String, Node>?
        get() = rootInActiveWindow?.run {
            try {
                createNodeMap(this) { node ->
                    nodeHandler.generateNodeHash(node)
                }
            } catch (e: Exception) {
                e.logE(TAG)
                null
            } finally {
                recycle()
            }
        }

    override val currentApp: Pair<String, String>?
        get() = iForeground.currentApp


    override fun onServiceConnected() {
        logD(TAG, "无障碍服务已连接")
        "无障碍服务已经连接".toast()
        instance = this
        nodeHandler = NodeHandler.bindTo(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when {
            event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                event.packageName?.toString()?.let { pkg ->
                    iForeground.update(pkg)
                }
            }
        }
    }

    /**
     * 根据哈西查找节点: 根据哈希计算的规则, 可以在一定时间内确保找到正确的节点
     */
    override suspend fun getNodeByHash(hash: String): AccessibilityNodeInfo? {
        return nodeHandler.findNodeByHash(hash)
    }

    override fun scrollScreen(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        startTime: Long,
        duration: Long,
        callback: GestureResultCallback?
    ): Boolean = scroll(startX, startY, endX, endY, startTime, duration, callback)


    override fun onUnbind(intent: Intent?): Boolean {
        logD(TAG, "无障碍服务已解绑")
        nodeHandler.unbind()
        instance = null
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        logD(TAG, "无障碍服务中断")
    }

    override fun onDestroy() {
        logD(TAG, "无障碍服务已销毁")
        super.onDestroy()
    }
}