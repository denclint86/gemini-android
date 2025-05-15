package com.tv.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.tv.utils.accessibility.Accessibility.Companion.SAVE_LIST
import com.tv.utils.accessibility.beans.Node
import com.zephyr.global_values.TAG
import com.zephyr.log.logE


/**
 * 模拟点击无障碍结点
 */
fun AccessibilityNodeInfo.click() =
    performAction(AccessibilityNodeInfo.ACTION_CLICK)

/**
 * 模拟长按无障碍结点
 */
fun AccessibilityNodeInfo.longClick() =
    performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)

/**
 * 调用无障碍节点对应 [android.view.View] 的 setText 函数
 */
fun AccessibilityNodeInfo.setTextTo(text: String): Boolean {
    val arguments = Bundle()
    arguments.putCharSequence(
        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
        text
    )
    return performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
}

/**
 * 控制一个可以滑动的控件向上（或向左）滑动
 */
fun AccessibilityNodeInfo.scrollForward() =
    performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)

/**
 * 控制一个可以滑动的控件向下（或向右）滑动
 */
fun AccessibilityNodeInfo.scrollBackward() =
    performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)

/**
 * 利用无障碍服务模拟返回按钮
 */
fun AccessibilityService.back() =
    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)

/**
 * 利用无障碍服务模拟手势滑动
 *
 * @param startTime 执行函数后多久后启动模拟手势
 * @param duration 模拟手势滑动的执行时间
 * @param callback 手势滑动结果回调
 */
fun AccessibilityService.scroll(
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    startTime: Long = 0L,
    duration: Long = 500L,
    callback: AccessibilityService.GestureResultCallback? = null
): Boolean {
    val path = Path()
    path.moveTo(startX, startY)
    path.lineTo(endX, endY)
    logE(TAG, "无障碍滚动: startX:${startX}, startY:$startY, endX:$endX, endY:$endY")
    val stroke = GestureDescription.StrokeDescription(path, startTime, duration)
    val gesture = GestureDescription.Builder()
        .addStroke(stroke)
        .build()
    return dispatchGesture(gesture, callback, null)
}

const val STRING_MAX_SIZE = 30

fun createNodeMap(
    rootNode: AccessibilityNodeInfo,
    hashMethod: (AccessibilityNodeInfo) -> String
): Map<String, Node> {
    val nodeMap = mutableMapOf<String, Node>()
    traverseNode(rootNode) { node ->
        val rect = Rect()
        node.getBoundsInScreen(rect)

        if (node.className.contains(SAVE_LIST)) {
            val hash = hashMethod(node)
            var str = node.text?.toString()

            if ((str?.length ?: 0) > STRING_MAX_SIZE) {
                str = str!!.take(STRING_MAX_SIZE) + "..."
            }

            nodeMap[hash] = Node(str, node, rect)
        }
    }
    return nodeMap
}

/**
 * 对父节点进行递归操作
 */
fun traverseNode(
    node: AccessibilityNodeInfo?,
    shouldRecycle: Boolean = true,
    action: (AccessibilityNodeInfo) -> Unit
) {
    if (node == null) return
    try {
        action(node)
    } catch (t: Throwable) {
        t.logE("Access")
    }

    for (i in 0 until node.childCount) {
        node.getChild(i)?.let { childNode ->
            traverseNode(childNode, shouldRecycle, action)
            if (shouldRecycle)
                childNode.recycle()
        }
    }
}
