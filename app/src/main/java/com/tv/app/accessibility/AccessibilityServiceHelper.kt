package com.tv.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.os.Handler
import android.view.accessibility.AccessibilityNodeInfo
import com.zephyr.log.logE

const val CLS_VIEW_GROUP = "android.view.ViewGroup"
const val CLS_FRAME_LAYOUT = "android.widget.FrameLayout"
const val CLS_SCROLL_VIEW = "android.widget.ScrollView"
const val CLS_LINEAR_LAYOUT = "android.widget.LinearLayout"
const val CLS_RELATIVE_LAYOUT = "android.widget.RelativeLayout"
const val CLS_LIST_VIEW = "android.widget.ListView"
const val CLS_RECYCLER_VIEW = "androidx.recyclerview.widget.RecyclerView"

const val CLS_TEXT_VIEW = "android.widget.TextView"
const val CLS_BUTTON = "android.widget.Button"
const val CLS_IMAGE_VIEW = "android.widget.ImageView"
const val CLS_IMAGE_BUTTON = "android.widget.ImageButton"

/**
 * 模拟点击无障碍结点
 *
 * @return 若点击成功，返回 true ，否则返回 false
 */
fun AccessibilityNodeInfo.click() =
    performAction(AccessibilityNodeInfo.ACTION_CLICK)

/**
 * 模拟长按无障碍结点
 *
 * @return 若长按成功，返回 true ，否则返回 false
 */
fun AccessibilityNodeInfo.longClick() =
    performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)

/**
 * 调用无障碍节点对应 [android.view.View] 的 setText 函数
 *
 * @return 若设置成功，返回 true ，否则返回 false
 */
fun AccessibilityNodeInfo.text(text: String): Boolean {
    val arguments = Bundle()
    arguments.putCharSequence(
        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
        text
    )
    return performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
}

/**
 * 控制一个可以滑动的控件向上（或向左）滑动
 *
 * @return 若滑动成功，返回 true ，否则返回 false
 */
fun AccessibilityNodeInfo.scrollForward() =
    performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)

/**
 * 控制一个可以滑动的控件向下（或向右）滑动
 *
 * @return 若滑动成功，返回 true ，否则返回 false
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
 * @param startX 手势开始位置的 X 坐标
 * @param startY 手势开始位置的 Y 坐标
 * @param endX 手势结束位置的 X 坐标
 * @param endY 手势结束位置的 Y 坐标
 * @param startTime 执行函数后多久后启动模拟手势
 * @param duration 模拟手势滑动的执行时间
 * @param callback 手势滑动结果回调
 * @param handler 指定模拟手势在指定的 [Handler] 上执行
 */
fun AccessibilityService.scroll(
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    startTime: Long = 500L,
    duration: Long = 500L,
    callback: AccessibilityService.GestureResultCallback? = null,
    handler: Handler? = null
) {
    val path = Path()
    path.moveTo(startX, startY)
    path.lineTo(endX, endY)
    logE("path", "startX${startX},startY$startY,endX$endX,endY$endY")
    val stroke = GestureDescription.StrokeDescription(path, startTime, duration)
    val gesture = GestureDescription.Builder()
        .addStroke(stroke)
        .build()
    dispatchGesture(gesture, callback, handler)
}

/**
 * 在指定结点及其子节点（包括自己）中寻找拥有指定特征的结点，若没有符合特征的结点，则返回空，
 * 注意只会返回找到的第一个结点
 * <p>
 *
 * 如果需要寻找多个相同特征的结点，请使用 [findNodes] 函数
 *
 * @param match 指定的特征
 */
fun AccessibilityNodeInfo.findNodeOrNull(match: AccessibilityNodeInfo.() -> Boolean): AccessibilityNodeInfo? {
    // 若当前结点符合要求，则返回当前结点
    if (match()) return this
    // 遍历所有直接子节点
    val childCount = childCount
    if (childCount != 0) {
        for (i in 0 until childCount) {
            val child = getChild(i) ?: continue
            val target = child.findNodeOrNull(match)
            if (target != null) {
                return target
            }
        }
    }
    // 当前结点和其所有直接与间接子节点均不符合要求
    return null
}

/**
 * 获取指定结点及其子节点（包括自己）中的所有符合指定特征的结点
 *
 * @param match 指定的特征
 *
 * @return 返回找到的所有结点，若没有，则返回一个空列表
 */
fun AccessibilityNodeInfo.findNodes(
    match: AccessibilityNodeInfo.() -> Boolean
): List<AccessibilityNodeInfo> =
    mutableListOf<AccessibilityNodeInfo>().also { findNodesInternal(match, it) }

private fun AccessibilityNodeInfo.findNodesInternal(
    match: AccessibilityNodeInfo.() -> Boolean,
    result: MutableList<AccessibilityNodeInfo>
) {
    // 若当前结点符合要求，则返回当前结点
    if (match()) result.add(this)
    // 遍历所有直接子节点
    val childCount = childCount
    if (childCount != 0) {
        repeat(childCount) {
            getChild(it)?.run { findNodesInternal(match, result) }
        }
    }
}

/**
 * 在指定结点的父节点节点（不包括自己）中寻找拥有指定特征的结点，若没有符合特征的结点，则返回空，
 * 注意只会返回找到的第一个结点
 *
 * @param match 指定的特征
 */
fun AccessibilityNodeInfo.findSuperNodeOrNull(match: AccessibilityNodeInfo.() -> Boolean): AccessibilityNodeInfo? {
    val sup = parent ?: return null
    return if (sup.match()) sup
    else sup.findSuperNodeOrNull(match)
}








