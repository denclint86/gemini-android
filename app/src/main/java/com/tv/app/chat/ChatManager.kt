package com.tv.app.chat


import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.tv.app.ApiModelProvider
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 依赖 api provider 实现循环切换 apikey
 */
object ChatManager {
    private var history = mutableListOf<Content>()

    private var chat = runBlocking { newChat() }
    private var chatScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    private suspend fun newModel() = ApiModelProvider.createModel()

    private suspend fun newChat(history: MutableList<Content> = mutableListOf()) =
        newModel().startChat(history = history)

    private suspend fun switchApikey() {
        history = chat.history
        chat = newModel().startChat(history)
    }

    suspend fun resetChat() {
        close()
        chat = newChat()
        open()
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

    private fun open() {
        chatScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        history.clear()
    }

    private fun close() {
        chatScope.cancel()
        history.clear()
    }
}