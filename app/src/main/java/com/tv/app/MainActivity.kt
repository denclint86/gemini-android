package com.tv.app

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.tv.app.chat.ChatViewModel
import com.tv.app.chat.GenerativeViewModelFactory
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

        viewModel.sendMessage("我在测试api,你现在应该有一个可用api,说说这是个什么样的api，然后分别使用'niki'和'asd'去调用他,最后告诉我你得到的结果")
    }
}