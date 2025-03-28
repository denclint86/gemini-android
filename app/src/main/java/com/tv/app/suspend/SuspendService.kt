package com.tv.app.suspend

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.tv.app.R

class SuspendService : Service() {
    private val binder = SuspendServiceBinder()

    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null // 悬浮窗View

    private var listener: ItemViewTouchListener.OnTouchEventListener? = null

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        initObserve()
    }

    private fun initObserve() {
        SuspendViewModel.isShowSuspendWindow.observeForever { isShow ->
            if (isShow) {
                showWindow()
            } else {
                floatRootView?.windowToken?.let {
                    runCatching {
                        windowManager.removeView(floatRootView)
                        floatRootView = null
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)

        val layoutParam = WindowManager.LayoutParams().apply {
            type =
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.START or Gravity.TOP
            x = outMetrics.widthPixels / 2 - width / 2 // 屏幕居中
            y = outMetrics.heightPixels / 2 - height / 2
        }

        floatRootView = LayoutInflater.from(this).inflate(R.layout.layout_suspend, null)

        // 跟手
        floatRootView?.setOnTouchListener(
            ItemViewTouchListener(
                layoutParam,
                windowManager,
                listenerImpl
            )
        )
        windowManager.addView(floatRootView, layoutParam)
    }

    override fun onDestroy() {
        super.onDestroy()
        floatRootView?.let {
            runCatching {
                windowManager.removeView(it)
            }
        }
    }

    // 代理
    private var listenerImpl = object : ItemViewTouchListener.OnTouchEventListener {
        override fun onClick() {
            listener?.onClick()
        }

        override fun onDrag() {
            listener?.onDrag()
        }

        override fun onLongPress() {
            listener?.onLongPress()
        }
    }

    inner class SuspendServiceBinder : Binder() {
        fun getView() = floatRootView

        fun setOnTouchEventListener(l: ItemViewTouchListener.OnTouchEventListener?) {
            listener = l
        }
    }
}