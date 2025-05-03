package com.tv.app.chat

import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.tv.app.chat.mvi.bean.ChatMessage
import kotlinx.coroutines.flow.Flow

// 用于解耦 viewmodel

interface IResponseProcessor {
    suspend fun processResponse(response: GenerateContentResponse, messageId: String): ProcessResult
    data class ProcessResult(
        val text: String,
        val functionCalls: List<Pair<String, Map<String, String?>?>>
    )
}

interface IFunctionCallHandler {
    suspend fun handleFunctionCalls(
        functionCalls: List<Pair<String, Map<String, String?>?>>,
        onNewMessage: (ChatMessage) -> Unit
    ): List<Pair<String, Map<String, String?>?>>
}

interface IStreamHandler {
    suspend fun handleStream(
        stream: Flow<GenerateContentResponse>,
        messageId: String,
        onUpdate: (String, Boolean) -> Unit
    )
}

interface IUIUpdater {
    fun updateMessageText(messageId: String, text: String, isPending: Boolean)
    fun addMessage(message: ChatMessage)
}