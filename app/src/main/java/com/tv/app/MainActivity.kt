package com.tv.app

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.tv.app.chat.ChatViewModel
import com.tv.app.chat.GenerativeViewModelFactory
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.databinding.ActivityMainBinding
import com.zephyr.vbclass.ViewBindingActivity

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    private lateinit var viewModel: ChatViewModel

    override fun ActivityMainBinding.initBinding() {
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this@MainActivity,
            GenerativeViewModelFactory
        )[ChatViewModel::class.java]

        val order =
            "我在测试api,你现在应该有一个可用api,描述这是个什么样的api，然后分别使用`niki`,`tom`,`den`和`jay`去调用他,最后告诉我你得到的结果"
        viewModel.sendIntent(ChatIntent.Chat(order))
    }
}