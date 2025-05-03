package com.tv.app.model

import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.flow.Flow


interface IChatManager {
    val isActive: Boolean
    suspend fun switchApiKey()
    suspend fun recreateModel()
    suspend fun resetChat()
    suspend fun sendMsg(content: Content): GenerateContentResponse
    suspend fun sendMsgStream(content: Content): Flow<GenerateContentResponse>
}