package com.tv.app.chat

import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.chat.mvi.ChatState
import com.tv.app.chat.mvi.bean.ChatMessage
import com.tv.app.chat.mvi.bean.funcMsg
import com.tv.app.chat.mvi.bean.modelMsg
import com.tv.app.chat.mvi.bean.systemMsg
import com.tv.app.chat.mvi.bean.userMsg
import com.tv.app.func.FuncManager
import com.tv.app.gemini.SYSTEM_PROMPT
import com.tv.app.gemini.createContent
import com.tv.app.gemini.userContent
import com.zephyr.extension.mvi.MVIViewModel
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.log.logI
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
    private val generativeModel: GenerativeModel
) : MVIViewModel<ChatIntent, ChatState, ChatEffect>() {
    private val chatManager: ChatManager by lazy { ChatManager(generativeModel) }
    private val _uiState: MutableStateFlow<ChatState> = MutableStateFlow(initUiState())
    val uiState: StateFlow<ChatState> = _uiState.asStateFlow()

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.Chat -> chat(intent.text)
            ChatIntent.ResetChat -> resetChat()
        }
    }

    override fun initUiState(): ChatState = ChatState(
        listOf(
            systemMsg(SYSTEM_PROMPT)
        )
    )

    private fun resetChat() {
        chatManager.resetChat()
    }

    private fun chat(text: String) = viewModelScope.launch(Dispatchers.IO) {
        logI(TAG, "user:\n$text")
        val userMessage = userMsg(text, true)
        updateState { modifyList { add(userMessage) } }

        val flow = chatManager.sendMsgStream(createContent { text(text) })

        updateState { setLastPending() }

        val modelMsg = modelMsg("", true)
        updateState { modifyList { add(modelMsg) } }

        processResponseFlow(flow, modelMsg)
    }

    private suspend fun processResponseFlow(
        flow: Flow<GenerateContentResponse>,
        modelMsg: ChatMessage
    ) {
        var responseText = ""
        val funcCalls = mutableListOf<Pair<String, Map<String, Any?>>>()

        flow.catch { e ->
            logE(TAG, chatManager.getHistory().toPrettyJson())
            e.logE(TAG)
        }.collect { chunk ->
            responseText += chunk.text
            updateState {
                modifyMsg(modelMsg.id) { copy(text = responseText) }
            }
            funcCalls.addAll(chunk.functionCalls.map { Pair(it.name, it.args) })
        }

        updateState { setLastPending() }

        logI(TAG, "llm:\n$responseText")
        if (funcCalls.isNotEmpty()) {
            handleFunctionCalls(funcCalls)
        }
    }

    private suspend fun handleFunctionCalls(funcCalls: List<Pair<String, Map<String, Any?>>>) {
        // 统一执行函数再发送
        val resultsBuilder = StringBuilder()
        withContext(Dispatchers.IO) {
            funcCalls.map { (name, args) ->
                FuncManager.executeFunction(name, args).also { result ->
                    resultsBuilder.append("$name($args): $result\n")
                }
            }
        }

        val jsons = resultsBuilder.toString()

        updateState {
            modifyList { add(funcMsg(jsons, true)) }
        }

        logI(TAG, "tools:\n$jsons")

        val funcResponse = chatManager.sendMsgStream(
            userContent { text(jsons) }
        )

        updateState { setLastPending() }

        val responseMsg = modelMsg("", true)
        updateState {
            modifyList { add(responseMsg) }
        }

        var funcResponseText = ""
        val newFunCalls = mutableListOf<Pair<String, Map<String, Any?>>>()

        funcResponse.catch { e ->
            logE(TAG, chatManager.getHistory().toPrettyJson())
            e.logE(TAG)
        }.collect { chunk ->
            funcResponseText += chunk.text ?: ""
            updateState {
                modifyMsg(responseMsg.id) { copy(text = funcResponseText) }
            }
            newFunCalls.addAll(chunk.functionCalls.map {
                Pair(it.name, it.args)
            })
        }

        updateState { setLastPending() }

        if (funcResponseText.isNotEmpty()) {
            logI(TAG, "llm:\n$funcResponseText")
        }

        logE(TAG, "第二次函数数量: ${newFunCalls.size}")
        if (newFunCalls.isNotEmpty()) {
            handleFunctionCalls(newFunCalls)
        }
    }
}