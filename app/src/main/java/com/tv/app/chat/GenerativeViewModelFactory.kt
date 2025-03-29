package com.tv.app.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.tv.app.APP_TOOLS
import com.tv.app.MODEL_NAME
import com.tv.app.R
import com.tv.app.SYSTEM_PROMPT
import com.tv.app.GEMINI_CONFIG
import com.zephyr.global_values.globalContext

/**
 * 自定义 vm 工厂，内部注入 Gemini 实例
 */
val GenerativeViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {

        return with(modelClass) {
            when {
                isAssignableFrom(ChatViewModel::class.java) -> {
                    val generativeModel = GenerativeModel(
                        modelName = MODEL_NAME,
                        apiKey = globalContext!!.getString(R.string.api_key),
                        systemInstruction = content { text(SYSTEM_PROMPT) },
                        tools = APP_TOOLS,
                        generationConfig = GEMINI_CONFIG
                    )
                    ChatViewModel(generativeModel)
                }

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
    }
}