package com.tv.app.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.tv.app.APP_TOOLS
import com.tv.app.MODEL_NAME
import com.tv.app.R
import com.tv.app.SYSTEM_PROMPT
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
            temperature = 0.3f       // 较低温度，输出更确定、更聪明
            maxOutputTokens = 2048   // 允许更长的响应，提升上下文理解和推理能力
            topP = 0.95f            // 核采样，保持连贯性
            topK = 40               // 限制 token 选择范围，提升质量
            candidateCount = 1      // 只返回一个最佳候选
        }

        return with(modelClass) {
            when {
                isAssignableFrom(ChatViewModel::class.java) -> {
                    val generativeModel = GenerativeModel(
                        modelName = MODEL_NAME,
                        apiKey = globalContext!!.getString(R.string.api_key),
                        systemInstruction = content { text(SYSTEM_PROMPT) },
                        tools = APP_TOOLS,
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