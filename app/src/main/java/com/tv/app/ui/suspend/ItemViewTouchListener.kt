package com.tv.app.ui.suspend

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

class ItemViewTouchListener(
    private val layoutParams: WindowManager.LayoutParams,
    private val windowManager: WindowManager,
    private val listener: OnTouchEventListener? = null // 添加回调接口
) : View.OnTouchListener {
    companion object {
        private const val DRAG_THRESHOLD = 8 // 拖动阈值，超过视为拖动
        private const val LONG_PRESS_THRESHOLD = 600L // 长按时间阈值
    }

    private var initialX = 0f // 初始触摸点 X
    private var initialY = 0f // 初始触摸点 Y
    private var lastX = 0 // 上一次移动的 X
    private var lastY = 0 // 上一次移动的 Y
    private var startTime = 0L // 触摸开始时间
    private var isDragging = false // 是否正在拖动

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
                }
            }

            MotionEvent.ACTION_UP -> {
                // 计算触摸持续时间
                val duration = System.currentTimeMillis() - startTime

                // 如果没有拖动，则判断是短按还是长按
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

    // 回调接口定义
    interface OnTouchEventListener {
        fun onClick()
        fun onDrag()
        fun onLongPress()
    }
}