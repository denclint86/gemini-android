package com.tv.app

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.tv.app.chat.ChatViewModel
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.databinding.ActivityMainBinding
import com.tv.app.func.FuncManager
import com.tv.app.func.models.VisibleViewsModel
import com.tv.app.ui.ChatAdapter
import com.zephyr.extension.ui.PreloadLayoutManager
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preloadLayoutManager: PreloadLayoutManager

    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    private fun testFunc() {
        GlobalScope.launch {
            delay(5000)
            val r = FuncManager.executeFunction(
//                ScrollModel.name, mapOf(
//                    "startX" to "600",
//                    "startY" to "2000",
//                    "endX" to "600",
//                    "endY" to "500",
//                    "duration" to "800",
//                )
//                ClickModel.name, mapOf(
//                    "x" to "600",
//                    "y" to "2000"
//                )
//                SetTextModel.name, mapOf("text" to "你 a 😄")
                VisibleViewsModel.name, mapOf()
            )
            logE(TAG, r)
        }
    }

    override fun ActivityMainBinding.initBinding() {
        enableEdgeToEdge()
        openOverlaySetting()
        setUpScreenCapture()

        chatAdapter = ChatAdapter()
        preloadLayoutManager = PreloadLayoutManager(this@MainActivity, RecyclerView.VERTICAL)
        viewModel = ViewModelProvider(this@MainActivity)[ChatViewModel::class.java]

        testFunc()

        rv.adapter = chatAdapter
        rv.layoutManager = preloadLayoutManager
        (rv.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        val order = "在谷歌商店安装“duolingo”"
        et.setText(order)

        btn.setOnClickListener {
            val text = et.text.toString()
            if (text.isBlank())
                "输入不可为空".toast()
            else
                viewModel.sendIntent(ChatIntent.Chat(text))
        }

        registerMVI()
    }

    private fun registerMVI() {
        viewModel.observeState {
            lifecycleScope.launch {
                map { it.messages }.collect { list ->
                    chatAdapter.submitList(list)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiEffectFlow.collect { effect ->
                when (effect) {
                    is ChatEffect.ChatSent -> lifecycleScope.launch {
                        delay(100)
                        if (effect.shouldClear)
                            binding.et.setText("")
                        binding.rv.smoothScrollToPosition(chatAdapter.itemCount - 1)
                    }

                    is ChatEffect.Generating -> "请等待当前回答结束".toast()
                    is ChatEffect.Error -> effect.t?.message.toast()
                }
            }
        }
    }

    private fun setUpScreenCapture() {
        if (App.binder.get()?.isScreenCaptureEnabled() == true) return
        val screenCaptureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.let {
                        App.binder.get()?.setupScreenCapture(result.resultCode, it)
                    }
                    logE(TAG, "已授权屏幕获取")
                } else {
                    logE(TAG, "已拒绝屏幕获取")
                }
            }

        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun openOverlaySetting() {
        if (!hasOverlayPermission()) {
            overlayPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (!hasOverlayPermission()) {
                        "悬浮窗权限未开启".toast()
                    }
                }
            overlayPermissionLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }
    }
}