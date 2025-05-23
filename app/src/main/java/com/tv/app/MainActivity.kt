package com.tv.app

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.marginBottom
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.tv.app.chat.ChatViewModel
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.chat.mvi.collectFlow
import com.tv.app.chat.mvi.observe
import com.tv.app.databinding.ActivityMainBinding
import com.tv.app.view.EditTextActivity
import com.tv.app.view.SettingsActivity
import com.tv.app.view.suspendview.SuspendViewService.Companion.binder
import com.tv.app.view.ui.ChatAdapter
import com.tv.utils.CallbackWaiter
import com.tv.utils.Role
import com.tv.utils.createViewModel
import com.tv.utils.findViewAtPoint
import com.tv.utils.hasOverlayPermission
import com.tv.utils.keyborad.IKeyboardUtil
import com.tv.utils.keyborad.KeyboardUtil
import com.tv.utils.parentIs
import com.tv.utils.setViewInsets
import com.tv.utils.startActivity
import com.zephyr.extension.ui.PreloadLayoutManager
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.scaling_layout.State
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * 示例：为自定义回调接口提供扩展
 */
interface CustomCallback<T> {
    fun onSuccess(result: T)
    fun onFailure(error: Throwable)
}

suspend fun <T> awaitCustomCallback(
    timeoutMs: Long? = 30_000,
    triggerAction: (CustomCallback<T>) -> Unit
): T? {
    return CallbackWaiter.await(timeoutMs) {
        val callback = object : CustomCallback<T> {
            override fun onSuccess(result: T) {
                onResult(result)
            }

            override fun onFailure(error: Throwable) {
                onError(error)
            }
        }

        registerCallback {
            triggerAction(callback)
        }
    }
}

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preloadLayoutManager: PreloadLayoutManager

    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>

    private lateinit var keyboardUtil: IKeyboardUtil

    private var btnAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // 避免在 application 实例化时启动服务, 导致服务自动复活
        App.startSuspendService()
        initLaunchers()

        super.onCreate(savedInstanceState)
    }

    override fun ActivityMainBinding.initBinding() {
        enableEdgeToEdge()
        openOverlaySettingIfNeeded()
        setUpScreenCapture()

        chatAdapter = ChatAdapter()
        preloadLayoutManager = PreloadLayoutManager(this@MainActivity, RecyclerView.VERTICAL)
        viewModel = this@MainActivity.createViewModel<ChatViewModel>()

        setSupportActionBar(toolBar)
        setInserts()

        rv.adapter = chatAdapter
        rv.layoutManager = preloadLayoutManager
        (rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        btnChat.setOnNewStateListener { state ->
            if (state == State.PROGRESSING) return@setOnNewStateListener
        }

        btnChat.setOnSubmitListener { str ->
            if (str.isBlank()) {
                "输入不可为空".toast()
            } else {
                viewModel.sendIntent(ChatIntent.Chat(str))
            }
            false
        }

        keyboardUtil = KeyboardUtil.attach(btnChat.editText, this@MainActivity)

        btnChat.setIFocusHandler(keyboardUtil.getFocusHandler())

        keyboardUtil.state.observe(this@MainActivity) { state ->
            if (state is IKeyboardUtil.State.Hidden) {
                btnChat.collapse()
            } else {
                btnChat.expand()
            }
        }

        keyboardUtil.height.observe(this@MainActivity) { height ->
            val h = height ?: 0

            val extraMinus = if (h == 0) 0 else btnChat.marginBottom
            val targetTranslationY = extraMinus - h.toFloat()

            btnAnimator?.cancel()
            btnAnimator = ObjectAnimator.ofFloat(
                btnChat,
                "translationY",
                btnChat.translationY,
                targetTranslationY
            ).apply {
                duration = 220
                interpolator = DecelerateInterpolator()
            }
            btnAnimator?.start()
        }

        registerMVI()
    }

    override fun onDestroy() {
        binding.btnChat.setIFocusHandler(null)
        keyboardUtil.detach()
        btnAnimator?.cancel()
        super.onDestroy()
    }

    private fun setInserts() = binding.run {
        btnChat.setViewInsets { insets ->
            bottomMargin = insets.bottom
        }
        toolBar.setViewInsets { insets ->
            topMargin = insets.top
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                startActivity<SettingsActivity>()
            }

            R.id.reset ->
                viewModel.sendIntent(ChatIntent.ResetChat)

            else -> {
                viewModel.sendIntent(ChatIntent.LogHistory)
                startActivity<EditTextActivity>()
            }
        }
        return super.onOptionsItemSelected(item);
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            binding.btnChat.run {
                val touchedView = findViewAtPoint(event.x, event.y)
                touchedView?.let { view ->
                    if (state == State.EXPANDED && !view.parentIs(frameLayout))
                        collapse()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun registerMVI() {
        viewModel.observe(lifecycleScope, { it.messages }) {
            val list = withContext(Dispatchers.IO) {
                if (ChatAdapter.HIDE_ENABLED)
                    it.filter { msg -> msg.role != Role.SYSTEM }
                else
                    it
            }
            chatAdapter.submitList(list)
        }

        lifecycleScope.launch {
            viewModel.collectFlow { effect ->
                when (effect) {
                    is ChatEffect.ChatSent -> {
                        delay(100)
                        if (effect.shouldClear)
                            binding.btnChat.editText.setText("")
                        binding.rv.smoothScrollToPosition(chatAdapter.itemCount - 1)
                    }

                    is ChatEffect.Generating -> "请等待当前回答结束".toast()
                    is ChatEffect.Error -> effect.t?.message.toast()
                    ChatEffect.Done -> "已完成".toast()
                }
            }
        }
    }

    private fun setUpScreenCapture() {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun openOverlaySettingIfNeeded() {
        if (hasOverlayPermission()) return
        overlayPermissionLauncher.launch(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
        )
    }

    private fun initLaunchers() {
        overlayPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (!hasOverlayPermission()) {
                    "悬浮窗权限未开启".toast()
                } else {
                    binder?.suspendViewManager?.rootVisibility = View.VISIBLE
                }
            }
        screenCaptureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val captureManager =
                    binder?.captureManager ?: return@registerForActivityResult
                if (captureManager.isAvailable) return@registerForActivityResult

                if (result.resultCode == RESULT_OK) {
                    result.data?.let {
                        captureManager.setup(result.resultCode, it)
                    }
                    logE(TAG, "已授权屏幕获取")
                } else {
                    logE(TAG, "已拒绝屏幕获取")
                }
            }
    }
}