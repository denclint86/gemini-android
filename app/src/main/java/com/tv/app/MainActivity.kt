package com.tv.app

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.tv.app.databinding.ActivityMainBinding
import com.zephyr.log.toLogString
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    override fun ActivityMainBinding.initBinding() {
        enableEdgeToEdge()


        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyC6PC29ZVvivYSOlUDYm2lILfqJsOtfyc4"
        )


        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val responseFlow =
                    generativeModel.generateContentStream(prompt = "this is an api test, intro yourself")
                var currentText = ""
                responseFlow.collect { chunk ->
                    chunk.text?.let { text ->
                        currentText += text
                        println(text)
                    }
                }
            } catch (t: Throwable) {
                println(t.toLogString())
            }
        }
    }
}