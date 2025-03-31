package com.tv.app.chat


import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.tv.app.ApiProvider
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

/**
 * 依赖 api provider 实现循环切换 apikey
 */
object ChatManager {
    private var history = mutableListOf<Content>()

    private var chat = newChat()
    private val chatScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    private val generativeModel: GenerativeModel
        get() = ApiProvider.createModel()

    private fun newChat(history: MutableList<Content> = mutableListOf()) =
        generativeModel.startChat(history = history)

    private fun switchApikey() {
        history = chat.history
        chat = generativeModel.startChat(history)
    }

    fun resetChat() {
        close()
        chat = newChat()
        history.clear()
    }

    fun isActive(): Boolean = mutex.isLocked

    suspend fun sendMsg(content: Content): GenerateContentResponse =
        withContext(chatScope.coroutineContext) {
            logE(TAG, "普通请求被调用")
            mutex.withLock {
                logE(TAG, "普通请求取得锁")
                switchApikey()
                chat.sendMessage(content)
            }
        }

    suspend fun sendMsgStream(content: Content): Flow<GenerateContentResponse> =
        withContext(chatScope.coroutineContext) {
            logE(TAG, "流式请求被调用")
            mutex.withLock {
                logE(TAG, "流式请求取得锁")
                switchApikey()
                chat.sendMessageStream(content)
            }
        }

    private fun close() {
        chatScope.cancel()
        history.clear()
    }
}