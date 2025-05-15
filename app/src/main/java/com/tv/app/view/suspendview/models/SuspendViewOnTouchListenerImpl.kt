package com.tv.app.view.suspendview.models

import android.annotation.SuppressLint
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.tv.app.view.suspendview.interfaces.SuspendViewEventCallback
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 悬浮窗触摸事件监听器, 处理拖动、点击和长按事件
 * 确保悬浮窗不会被拖出屏幕范围
 */
class SuspendViewOnTouchListenerImpl(
    private val layoutParams: WindowManager.LayoutParams,
    private val windowManager: WindowManager,
    private val listener: SuspendViewEventCallback? = null
) : View.OnTouchListener {
    companion object {
        private const val DRAG_THRESHOLD = 8 // 拖动阈值, 超过视为拖动
        private const val LONG_PRESS_THRESHOLD = 600L // 长按时间阈值
    }

    private var initialX = 0f // 初始触摸点 X
    private var initialY = 0f // 初始触摸点 Y
    private var lastX = 0 // 上一次移动的 X
    private var lastY = 0 // 上一次移动的 Y
    private var startTime = 0L // 触摸开始时间
    private var isDragging = false // 是否正在拖动

    // 获取屏幕尺寸
    private val displayMetrics = DisplayMetrics()
    private val screenWidth: Int
    private val screenHeight: Int

    // 估计的悬浮窗尺寸
    private var viewWidth = 0
    private var viewHeight = 0

    init {
        // 获取屏幕尺寸
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        // 首次触摸时记录视图尺寸
        if (viewWidth == 0 || viewHeight == 0) {
            viewWidth = view.width
            viewHeight = view.height
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录初始触摸位置和时间
                initialX = event.rawX
                initialY = event.rawY
                lastX = initialX.toInt()
                lastY = initialY.toInt()
                startTime = System.currentTimeMillis()
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val nowX = event.rawX.toInt()
                val nowY = event.rawY.toInt()
                val movedX = nowX - lastX
                val movedY = nowY - lastY

                // 计算从初始点开始的总移动距离
                val distanceX = abs(event.rawX - initialX)
                val distanceY = abs(event.rawY - initialY)

                // 如果移动距离超过阈值, 视为拖动
                if (distanceX > DRAG_THRESHOLD || distanceY > DRAG_THRESHOLD) {
                    isDragging = true

                    // 计算新位置
                    var newX = layoutParams.x + movedX
                    var newY = layoutParams.y + movedY

                    // 限制悬浮窗不超出屏幕边界
                    // 为了确保悬浮窗至少有一部分在屏幕内, 预留边缘的可视区域
                    val minVisiblePortion = 50 // 至少50px在屏幕内

                    // 限制X轴坐标范围
                    newX = when {
                        // 如果视图宽度小于屏幕宽度, 则限制在屏幕内
                        viewWidth < screenWidth -> {
                            max(
                                minVisiblePortion - viewWidth,
                                min(newX, screenWidth - minVisiblePortion)
                            )
                        }
                        // 如果视图宽度大于等于屏幕宽度, 则允许完全滑出, 但至少保留minVisiblePortion
                        else -> {
                            max(minVisiblePortion - viewWidth, min(newX, 0))
                        }
                    }

                    // 限制Y轴坐标范围
                    newY = when {
                        // 如果视图高度小于屏幕高度, 则限制在屏幕内
                        viewHeight < screenHeight -> {
                            max(
                                minVisiblePortion - viewHeight,
                                min(newY, screenHeight - minVisiblePortion)
                            )
                        }
                        // 如果视图高度大于等于屏幕高度, 则允许完全滑出, 但至少保留minVisiblePortion
                        else -> {
                            max(minVisiblePortion - viewHeight, min(newY, 0))
                        }
                    }

                    // 更新布局参数
                    layoutParams.x = newX
                    layoutParams.y = newY

                    // 更新视图布局
                    windowManager.updateViewLayout(view, layoutParams)

                    // 更新最后位置
                    lastX = nowX
                    lastY = nowY

                    // 触发拖动回调
                    listener?.onDrag()
                }
            }

            MotionEvent.ACTION_UP -> {
                // 计算触摸持续时间
                val duration = System.currentTimeMillis() - startTime

                // 如果没有拖动, 则判断是短按还是长按
                if (!isDragging) {
                    if (duration >= LONG_PRESS_THRESHOLD) {
                        listener?.onLongPress()
                    } else {
                        listener?.onClick()
                    }
                }
            }
        }
        return true // 消费事件
    }
}