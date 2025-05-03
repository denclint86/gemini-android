package com.tv.app

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.marginBottom
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.tv.app.chat.ChatViewModel
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.databinding.ActivityMainBinding
import com.tv.app.keyborad.KeyboardObserver
import com.tv.app.settings.SettingsActivity
import com.tv.app.ui.ChatAdapter
import com.tv.app.utils.collectFlow
import com.tv.app.utils.findViewAtPoint
import com.tv.app.utils.hasOverlayPermission
import com.tv.app.utils.observe
import com.tv.app.utils.parentIs
import com.tv.app.utils.setViewInsets
import com.zephyr.extension.ui.PreloadLayoutManager
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.scaling_layout.State
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preloadLayoutManager: PreloadLayoutManager

    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun ActivityMainBinding.initBinding() {
        enableEdgeToEdge()
//        openOverlaySetting()
//        setUpScreenCapture()

        chatAdapter = ChatAdapter()
        preloadLayoutManager = PreloadLayoutManager(this@MainActivity, RecyclerView.VERTICAL)
        viewModel = ViewModelProvider(this@MainActivity)[ChatViewModel::class.java]

        setSupportActionBar(toolBar)
        setInserts()

//        testFunc()

        rv.adapter = chatAdapter
        rv.layoutManager = preloadLayoutManager
        (rv.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        btnChat.collapse()
        btnChat.setText("对话")
        btnChat.setOnSubmitListener { str ->
            if (str.isBlank()) {
                "输入不可为空".toast()
            } else {
                viewModel.sendIntent(ChatIntent.Chat(str))
            }
            false
        }

        val keyboardObserver = KeyboardObserver.attach(this@MainActivity)
        keyboardObserver.keyboardState.observe(this@MainActivity) { state ->
            if (state == KeyboardObserver.State.Visible) {
                btnChat.expand()
            } else {
                btnChat.collapse()
            }
        }
        keyboardObserver.keyboardHeight.observe(this@MainActivity) { height ->
            val extraMinus = if (height == 0) 0 else btnChat.marginBottom
            btnChat.translationY = extraMinus - height.toFloat()
        }

        registerMVI()
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendIntent(ChatIntent.ReloadChat)
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
                val i = Intent(this, SettingsActivity::class.java)
                startActivity(i)
            }

            R.id.reset ->
                viewModel.sendIntent(ChatIntent.ResetChat)

            else ->
                ">_<".toast()
        }
        return super.onOptionsItemSelected(item);
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
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
            chatAdapter.submitList(it)
        }

        lifecycleScope.launch {
            viewModel.collectFlow { effect ->
                when (effect) {
                    is ChatEffect.ChatSent -> lifecycleScope.launch {
                        delay(100)
                        if (effect.shouldClear)
                            binding.btnChat.editText.setText("")
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