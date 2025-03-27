package com.tv.app.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.tv.app.R
import com.tv.app.func.tool.AppTools
import com.zephyr.global_values.globalContext

/**
 * 自定义 vm 工厂，内部注入 Gemini 实例
 */
val GenerativeViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val config = generationConfig {
            temperature = 0.7f
//            maxOutputTokens=2048
        }

        return with(modelClass) {
            when {

                isAssignableFrom(ChatViewModel::class.java) -> {
                    val generativeModel = GenerativeModel(
                        modelName = MODEL_NAME,
                        apiKey = globalContext!!.getString(R.string.api_key),
                        systemInstruction = content { text(SYSTEM_PROMPT) },
                        tools = AppTools,
                        generationConfig = config
                    )
                    ChatViewModel(generativeModel)
                }

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
    }
}