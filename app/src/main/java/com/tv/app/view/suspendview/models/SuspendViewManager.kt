package com.tv.app.view.suspendview.models

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.tv.app.R
import com.tv.app.databinding.LayoutSuspendViewBinding
import com.tv.app.view.suspendview.SuspendViewService
import com.tv.app.view.suspendview.SuspendViewService.Companion.alwaysActiveLifecycleOwner
import com.tv.app.view.suspendview.interfaces.ISuspendViewManager
import com.tv.app.view.suspendview.interfaces.SuspendViewEventCallback
import com.tv.utils.createLayoutParam
import com.zephyr.extension.thread.runOnMain

/**
 * 悬浮窗视图管理器, 实现ISuspendView接口, 负责控制悬浮窗的显示和隐藏
 */
class SuspendViewManager(private val context: Context) : ISuspendViewManager {
    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var binding: LayoutSuspendViewBinding? = null

    private var floatRootView: View? = null

    private var isShowing = false
    private var lastX = 0
    private var lastY = 0

    private var touchListener: SuspendViewEventCallback? = null

    // 获取屏幕宽高, 用于限制悬浮窗位置
    private val screenWidth: Int by lazy {
        val metrics = context.resources.displayMetrics
        metrics.widthPixels
    }

    private val screenHeight: Int by lazy {
        val metrics = context.resources.displayMetrics
        metrics.heightPixels
    }

    /**
     * 悬浮窗根视图的可见性
     */
    override var rootVisibility: Int
        get() = floatRootView?.visibility ?: View.GONE
        set(value) {
            runOnMain {
                if (value == View.VISIBLE && !isShowing) {
                    show()
                } else if (value == View.GONE && isShowing) {
                    hide()
                } else if (floatRootView != null) {
                    floatRootView?.visibility = value
                }
            }
        }

    /**
     * 悬浮窗进度条的可见性
     */
    override var progressBarVisibility: Int
        get() = binding?.progressBar?.visibility ?: View.GONE
        set(value) {
            runOnMain {
                binding?.progressBar?.visibility = value
            }
        }

    /**
     * 悬浮窗显示的文本内容
     */
//    override var text: String
//        get() = binding?.tvSuspendText?.text.toString()
//        set(value) {
//            SuspendViewService.setSuspendText(value)
//        }

    /**
     * 观察LiveData数据源, 将其值设置为悬浮窗文本
     */
//    override fun observeTo(liveData: LiveData<String>) {
//        liveData.observe(alwaysActiveLifecycleOwner) { text ->
//            this.text = text
//        }
//    }

    override fun setOnTouchEventListener(l: SuspendViewEventCallback?) {
        touchListener = l
    }

    /**
     * 限制坐标在屏幕范围内
     */
    private fun constrainPosition(x: Int, y: Int): Pair<Int, Int> {
        val constrainedX = x.coerceIn(0, screenWidth - 100) // 100是悬浮窗估计宽度
        val constrainedY = y.coerceIn(0, screenHeight - 100) // 100是悬浮窗估计高度
        return Pair(constrainedX, constrainedY)
    }

    /**
     * 显示悬浮窗
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun show() {
        if (isShowing) return

        // 限制位置在屏幕范围内
        val (safeX, safeY) = constrainPosition(lastX, lastY)
        lastX = safeX
        lastY = safeY

        val layoutParam = createLayoutParam(lastX, lastY)

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_suspend_view,
            null,
            false
        )

        binding?.service = (context as SuspendViewService)
        binding?.lifecycleOwner = alwaysActiveLifecycleOwner

        floatRootView = binding?.root
        floatRootView?.setOnTouchListener(
            SuspendViewOnTouchListenerImpl(
                layoutParam,
                windowManager,
                touchListener
            )
        )
        windowManager.addView(floatRootView, layoutParam)
        isShowing = true
    }

    /**
     * 隐藏悬浮窗
     */
    private fun hide() {
        if (!isShowing) return
        floatRootView?.windowToken?.let {
            runCatching {
                val lp = floatRootView?.layoutParams as? WindowManager.LayoutParams
                lp?.let {
                    // 保存位置信息, 并确保位置在屏幕范围内
                    val (safeX, safeY) = constrainPosition(lp.x, lp.y)
                    lastX = safeX
                    lastY = safeY
                }
                windowManager.removeView(floatRootView)
                floatRootView = null
                binding?.unbind()
                binding = null
                isShowing = false
            }
        }
    }

    /**
     * 释放所有资源, 移除悬浮窗
     */
    override fun release() {
        hide()
    }
}