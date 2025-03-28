package com.tv.app

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.tv.app.chat.ChatViewModel
import com.tv.app.chat.GenerativeViewModelFactory
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.databinding.ActivityMainBinding
import com.tv.app.ui.ChatAdapter
import com.zephyr.extension.ui.PreloadLayoutManager
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preloadLayoutManager: PreloadLayoutManager

    override fun ActivityMainBinding.initBinding() {
        enableEdgeToEdge()

        chatAdapter = ChatAdapter()
        preloadLayoutManager = PreloadLayoutManager(this@MainActivity, RecyclerView.VERTICAL)
        viewModel = ViewModelProvider(
            this@MainActivity,
            GenerativeViewModelFactory
        )[ChatViewModel::class.java]

        rv.adapter = chatAdapter
        rv.layoutManager = preloadLayoutManager
        (rv.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        val order = "帮我在play store安装多邻国"
        viewModel.sendIntent(ChatIntent.Chat(order))

        viewModel.observeState {
            lifecycleScope.launch {
                map { it.messages }.collect { list ->
                    logE(TAG, "collected")
                    chatAdapter.submitList(list)
                }
            }
        }
    }
}