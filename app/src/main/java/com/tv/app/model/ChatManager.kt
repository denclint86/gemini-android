package com.tv.app.model


import com.google.ai.client.generativeai.Chat
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
object ChatManager : ChatManagerImpl()


open class ChatManagerImpl : IChatManager {
    private var history = mutableListOf<Content>()

    private var chat = runBlocking { newChat() }
    private var chatScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()


    override val isActive: Boolean
        get() = mutex.isLocked

    override suspend fun switchApiKey() {
        history = chat.history
        chat = newModel().startChat(history)
    }

    override suspend fun recreateModel() {
        history = chat.history
        chat = newModel(false).startChat(history)
    }

    override suspend fun resetChat() {
        close()
        chat = newChat()
        open()
    }

    override suspend fun sendMsg(content: Content): GenerateContentResponse =
        withContext(chatScope.coroutineContext) {
            logE(TAG, "普通请求被调用")
            mutex.withLock {
                logE(TAG, "普通请求取得锁")
                switchApiKey()
                chat.sendMessage(content)
            }
        }

    override suspend fun sendMsgStream(content: Content): Flow<GenerateContentResponse> =
        withContext(chatScope.coroutineContext) {
            logE(TAG, "流式请求被调用")
            mutex.withLock {
                logE(TAG, "流式请求取得锁")
                switchApiKey()
                chat.sendMessageStream(content)
            }
        }

    private suspend fun newModel(newKey: Boolean = true) = ApiModelProvider.createModel(newKey)

    private suspend fun newChat(history: MutableList<Content> = mutableListOf()): Chat =
        newModel().startChat(history = history)

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