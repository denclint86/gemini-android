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
                logE(TAG, "普通请求取得信号量")
                chat.sendMessage(content)
            }
        }

    suspend fun sendMsgStream(content: Content): Flow<GenerateContentResponse> =
        withContext(chatScope.coroutineContext) {
            logE(TAG, "流式请求被调用")
            mutex.withLock {
                logE(TAG, "流式请求取得信号量")
                chat.sendMessageStream(content)
            }
        }

    private fun close() {
        chatScope.cancel()
    }
}

//
///**
// * 保证 chat 并发的绝对禁止
// */
//class ChatManager(
//    private val generativeModel: GenerativeModel
//) {
//    private var chat = createNewChat()
//
//    private val chatScope = CoroutineScope(SupervisorJob())
//    private val semaphore = Semaphore(1) // 信号量，控制并发
//
//    private var lastFlow: Flow<GenerateContentResponse>? = null // 保存最后一个流
//    private var isLastFlowActive: Boolean = false // 标记上一个流是否活跃
//
//    val isActive: Boolean
//        get() = isLastFlowActive
//
//    private fun createNewChat() = generativeModel.startChat(history = emptyList())
//
//    fun resetChat() {
//        close()
//        chat = createNewChat()
//    }
//
//    suspend fun sendMsg(content: Content): GenerateContentResponse =
//        withContext(chatScope.coroutineContext) {
//            semaphore.acquire() // 等待信号量
//            try {
//                chat.sendMessage(content)
//            } finally {
//                semaphore.release() // 释放信号量
//            }
//        }
//
//    suspend fun sendMsgStream(content: Content): Flow<GenerateContentResponse> =
//        withContext(chatScope.coroutineContext) {
//            logE(TAG, "可能将被阻塞")
//            // 检查上一个流是否活跃
//            while (isLastFlowActive) {
//                delay(30) // 等待上一个流完成
//            }
//
//            semaphore.acquire() // 等待信号量
//            logE(TAG, "结束阻塞")
//            try {
//                val flow = chat.sendMessageStream(content)
//                lastFlow = flow
//                isLastFlowActive = true
//
//                flow.onCompletion {
//                    isLastFlowActive = false
//                    semaphore.release() // 流结束后释放信号量
//                }.catch { e ->
//                    isLastFlowActive = false
//                    semaphore.release()
//                    throw e
//                }
//            } catch (e: Exception) {
//                isLastFlowActive = false
//                semaphore.release()
//                throw e
//            }
//        }
//
//    fun close() {
//        chatScope.cancel()
//    }
//}