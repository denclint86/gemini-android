package com.tv.app.ui.suspend

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlin.math.abs

class ItemViewTouchListener(
    private val layoutParams: WindowManager.LayoutParams,
    private val windowManager: WindowManager,
    private val listener: OnTouchEventListener? = null // 添加回调接口
) : View.OnTouchListener {
    private var initialX = 0f // 初始触摸点 X
    private var initialY = 0f // 初始触摸点 Y
    private var lastX = 0 // 上一次移动的 X
    private var lastY = 0 // 上一次移动的 Y
    private var startTime = 0L // 触摸开始时间
    private var isDragging = false // 是否正在拖动

    // 拖动阈值（单位：像素），超过此距离视为拖动
    private val DRAG_THRESHOLD = 10

    // 长按阈值（单位：毫秒），超过此时间视为长按
    private val LONG_PRESS_THRESHOLD = 500L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
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

                // 如果移动距离超过阈值，视为拖动
                if (distanceX > DRAG_THRESHOLD || distanceY > DRAG_THRESHOLD) {
                    isDragging = true
                    layoutParams.apply {
                        x += movedX
                        y += movedY
                    }
                    windowManager.updateViewLayout(view, layoutParams)
                    lastX = nowX
                    lastY = nowY
                    listener?.onDrag()
                    logE(TAG, "拖动中")
                }
            }

            MotionEvent.ACTION_UP -> {
                // 计算触摸持续时间
                val duration = System.currentTimeMillis() - startTime

                // 如果没有拖动，则判断是短按还是长按
                if (!isDragging) {
                    if (duration >= LONG_PRESS_THRESHOLD) {
                        listener?.onLongPress()
                        logE(TAG, "长按")
                    } else {
                        listener?.onClick()
                        logE(TAG, "点击")
                    }
                }
            }
        }
        return true // 消费事件
    }

    // 回调接口定义
    interface OnTouchEventListener {
        fun onClick()
        fun onDrag()
        fun onLongPress()
    }
}