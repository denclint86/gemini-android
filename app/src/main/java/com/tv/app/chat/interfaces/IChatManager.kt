package com.tv.app.chat.interfaces

import kotlinx.coroutines.flow.Flow


interface IChatManager<CONTENT, RESPONSE> {
    val isActive: Boolean
    val history: List<CONTENT>
    
    suspend fun switchApiKey()
    suspend fun recreateModel()
    suspend fun resetChat()
    suspend fun sendMsg(content: CONTENT): RESPONSE
    suspend fun sendMsgStream(content: CONTENT): Flow<RESPONSE>
}