package com.tv.app

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.tv.app.databinding.ActivityMainBinding
import com.tv.app.chat.ChatViewModel
import com.tv.app.chat.GenerativeViewModelFactory
import com.zephyr.vbclass.ViewBindingActivity

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    private lateinit var viewModel: ChatViewModel

    override fun ActivityMainBinding.initBinding() {
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this@MainActivity,
            GenerativeViewModelFactory
        )[ChatViewModel::class.java]

        viewModel.sendMessage("介绍习近平")
//        流式示例
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val responseFlow =
//                    generativeModel.generateContentStream(prompt = "this is an api test, intro yourself")
//                var currentText = ""
//                responseFlow.collect { chunk ->
//                    chunk.text?.let { text ->
//                        currentText += text
//                        println(text)
//                    }
//                }
//            } catch (t: Throwable) {
//                println(t.toLogString())
//            }
//        }
    }
}