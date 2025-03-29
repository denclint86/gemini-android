package com.tv.app.ui.suspend

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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.tv.app.R
import com.tv.app.databinding.LayoutSuspendBinding

class SuspendService : Service() {
    private val binder = SuspendServiceBinder()

    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null // 悬浮窗View
    private lateinit var binding: LayoutSuspendBinding

    private var listener: ItemViewTouchListener.OnTouchEventListener? = null

    // 创建一个永远处于RESUMED状态的LifecycleOwner
    private val alwaysActiveLifecycleOwner = object : LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this).apply {
            // 设置为RESUMED状态，确保LiveData始终更新
            currentState = Lifecycle.State.RESUMED
        }

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry
    }

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

        // 设置默认文本
        if (SuspendViewModel.suspendText.value.isNullOrEmpty()) {
            SuspendViewModel.suspendText.value = "悬浮"
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

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.layout_suspend,
            null,
            false
        )

        // 设置ViewModel和LifecycleOwner
        binding.viewModel = SuspendViewModel
        binding.lifecycleOwner = alwaysActiveLifecycleOwner

        floatRootView = binding.root

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
        fun getViewBinding() = binding

        fun setOnTouchEventListener(l: ItemViewTouchListener.OnTouchEventListener?) {
            listener = l
        }

        // 提供一个方法来更新悬浮窗文本
        fun updateSuspendText(text: String) {
            SuspendViewModel.suspendText.postValue(text)
        }
    }
}