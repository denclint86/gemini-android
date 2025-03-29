package com.tv.app.chat


import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ChatManager(
    private val generativeModel: GenerativeModel
) {
    private var chat = createNewChat()
    private val chatScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex() // 使用 Mutex 替代 Semaphore

    fun getHistory() = chat.history

    private fun createNewChat() = generativeModel.startChat(history = emptyList())

    fun resetChat() {
        close()
        chat = createNewChat()
    }

    fun isActive(): Boolean = mutex.isLocked

    suspend fun sendMsg(content: Content): GenerateContentResponse =
        withContext(chatScope.coroutineContext) {
            logE(TAG, "普通请求被调用")
            mutex.withLock {
                logE(TAG, "普通请求取得锁")
                chat.sendMessage(content)
            }
        }

    suspend fun sendMsgStream(content: Content): Flow<GenerateContentResponse> =
        withContext(chatScope.coroutineContext) {
            logE(TAG, "流式请求被调用")
            mutex.withLock {
                logE(TAG, "流式请求取得锁")
                chat.sendMessageStream(content)
            }
        }

    private fun close() {
        chatScope.cancel()
    }
}