package com.tv.app.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


enum class Role(val str: String) {
    SYSTEM("system"),
    USER("user"),
    MODEL("model")
}

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
    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    fun newChat() = generativeModel.startChat(
        history = listOf(
//            systemContent { text(SYSTEM_PROMPT) },
            userContent { text(SYSTEM_PROMPT) }
        )
    )

    fun resetUiState() {
        _uiState.value.removeAll()
        _uiState.value.addMessage(
            ChatMessage(
                text = SYSTEM_PROMPT,
//                role = Role.SYSTEM,
                role = Role.USER,
                isPending = false
            )
        )
    }

    fun sendMessage(userMessage: String) {
        // Add a pending message
        _uiState.value.addMessage(
            ChatMessage(
                text = userMessage,
                role = Role.USER,
                isPending = true
            )
        )

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)

                _uiState.value.replaceLastPendingMessage()

                response.text?.let { modelResponse ->
                    logE(TAG, modelResponse)
                    _uiState.value.addMessage(
                        ChatMessage(
                            text = modelResponse,
                            role = Role.MODEL,
                            isPending = false
                        )
                    )
                }
            } catch (e: Exception) {
                e.logE(TAG)
            }
        }
    }
}