package com.tv.app.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.tv.app.func.FuncManager
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


enum class Role(val str: String) {
    //    SYSTEM("system"),
    USER("user"),
    MODEL("model"),
    FUNC("function") // 不可用于请求
}

/**
 * 十分基础的 vm，待优化
 */
class ChatViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {
    private var chat = newChat()

    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(chat.history.map { content ->
            ChatMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                role = Role.entries.find { it.str == content.role }!!,
                isPending = false
            )
        }))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private fun newChat() = generativeModel.startChat(history = emptyList())

    fun sendMessage(userMessage: String) {
        _uiState.value.addMessage(
            ChatMessage(text = userMessage, role = Role.USER, isPending = true)
        )

        viewModelScope.launch {
            try {
                val response = chat.sendMessageStream(userMessage)
                _uiState.value.replaceLastPendingMessage()

                var responseText = ""
                val pendingFunctionCalls = mutableListOf<Pair<String, Map<String, String?>>>()

                // 收集 LLM 的响应和函数调用
                response.collect { chunk ->
                    responseText += chunk.text ?: ""
                    print(chunk.text)
                    chunk.functionCalls.forEach { func ->
                        pendingFunctionCalls.add(func.name to func.args)
                    }
                }

                // 显示 LLM 的初步响应
                if (responseText.isNotEmpty()) {
                    _uiState.value.addMessage(
                        ChatMessage(text = responseText, role = Role.MODEL, isPending = false)
                    )
                }

                // 处理所有函数调用并反馈结果
                if (pendingFunctionCalls.isNotEmpty()) {
                    pendingFunctionCalls.forEach { (name, args) ->
                        val result = FuncManager.executeFunction(name, args).toJson()

                        _uiState.value.addMessage(
                            ChatMessage(text = result, role = Role.FUNC, isPending = true)
                        )

                        // 将每个函数结果发送给 LLM
                        if (result.isBlank()) return@forEach
                        chat
                        val funcResponse = chat.sendMessageStream(content {
                            role = Role.USER.str
                            text(result)
                        })
                        _uiState.value.replaceLastPendingMessage()

                        var funcResponseText = ""
                        funcResponse.collect { chunk ->
                            print(chunk.text)
                            funcResponseText += chunk.text ?: ""
                        }

                        // 显示 LLM 对函数结果的响应
                        if (funcResponseText.isNotEmpty()) {
                            _uiState.value.addMessage(
                                ChatMessage(
                                    text = funcResponseText,
                                    role = Role.MODEL,
                                    isPending = false
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.logE(TAG)
                _uiState.value.addMessage(
                    ChatMessage(text = "Error: ${e.message}", role = Role.MODEL, isPending = false)
                )
            }
        }
    }
}