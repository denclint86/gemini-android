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

        val order =
//            "我在测试`function calling`,你现在应该有两个可用tool,描述这是个什么样的tool，然后分别使用`niki`,`tom`,`den`和`jay`去调用第一个函数（电话）,最后告诉我你得到的结果。然后再调用第二个，在获得结果后你应该继续调用剩余的函数，然后获取结果并告诉我。"
            "我在测试`function calling`,你现在应该有若干可用tool,描述这是个什么样的tool，现在测试一个简单的shell命令来打开play商店（通过am打开，加上超时参数）,最后调用toast来告诉我你得到的结果。然后再调用第二个，在获得结果后你应该继续调用剩余的函数，然后获取结果并告诉我。"
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