package com.tv.app.chat

import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.chat.mvi.ChatState
import com.tv.app.chat.mvi.bean.modelMsg
import com.tv.app.chat.mvi.bean.userMsg
import com.tv.app.func.FuncManager
import com.tv.app.gemini.createContent
import com.tv.app.gemini.userContent
import com.zephyr.extension.mvi.MVIViewModel
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.log.logI
import com.zephyr.net.toJson
import com.zephyr.net.toPrettyJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChatViewModel(
    private val generativeModel: GenerativeModel // Gemini 通信用
) : MVIViewModel<ChatIntent, ChatState, ChatEffect>() {
    private val chatManager: ChatManager by lazy { ChatManager(generativeModel) }

    private val _uiState: MutableStateFlow<ChatState> =
        MutableStateFlow(ChatState())
    val uiState: StateFlow<ChatState> = _uiState.asStateFlow()

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.Chat -> intent.apply {
                chat(text)
            }

            ChatIntent.ResetChat -> resetChat()
        }
    }

    override fun initUiState(): ChatState {
        return ChatState()
    }

    private fun resetChat() {
        chatManager.resetChat()
    }

    private fun chat(text: String) = viewModelScope.launch(Dispatchers.IO) {
        logI(TAG, "user: $text")
        val flow = createChatFlow(text)

        var responseText = ""
        val funcList = mutableListOf<Pair<String, Map<String, Any?>>>()

        val modelMsg = modelMsg("", true)
        updateState {
            modifyList {
                add(modelMsg)
            }
        }

        flow.catch { e ->
            logE(TAG, chatManager.getHistory().toPrettyJson())
            throw e
        }

        flow.collect { chunk ->
            responseText += chunk.text
            updateState {
                modifyMsg(modelMsg.id) {
                    copy(text = responseText)
                }
            }
            funcList.addAll(chunk.functionCalls.map { Pair(it.name, it.args) })
        }

        logI(TAG, "llm: $responseText")
        handleFuncCalls(funcList)
    }

    private suspend fun createChatFlow(text: String): Flow<GenerateContentResponse> {
        val msg = userMsg(text, true)
        updateState {
            modifyList { add(msg) }
        }

        val flow = chatManager.sendMsgStream(createContent { text(text) })
        updateState {
            setLastPending()
        }

        return flow
    }

    /**
     * 递归地处理所有函数请求
     */
    private suspend fun handleFuncCalls(list: List<Pair<String, Map<String, Any?>>>) {
        list.forEach { (name, args) ->
            val result = withContext(Dispatchers.IO) {
                FuncManager.executeFunction(name, args).toJson()
            }

            updateState {
                modifyList { add(userMsg(result, true)) }
            }

            // 将每个函数结果发送给 LLM
            val funcResponse = chatManager.sendMsgStream(
                userContent { text(result) }
            )

            updateState {
                setLastPending()
            }

            var funcResponseText = ""
            val funcList = mutableListOf<Pair<String, Map<String, Any?>>>()

            funcResponse.catch { e ->
                logE(TAG, chatManager.getHistory().toPrettyJson())
                throw e
            }

            // 收集响应并检查是否有新的函数调用
            funcResponse.collect { chunk ->
                funcResponseText += chunk.text ?: ""
                funcList.addAll(chunk.functionCalls.map { Pair(it.name, it.args) })
            }

            // 显示 LLM 对函数结果的响应
            if (funcResponseText.isNotEmpty()) {
                updateState {
                    modifyList { add(modelMsg(funcResponseText, false)) }
                }
                logI(TAG, "llm: $funcResponseText")
            }

            // 如果响应中包含新的函数调用，递归处理
            if (funcList.isNotEmpty()) {
                handleFuncCalls(funcList)
            }
        }
    }

}