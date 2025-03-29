package com.tv.app.chat

import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.tv.app.SYSTEM_PROMPT
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.chat.mvi.ChatState
import com.tv.app.chat.mvi.bean.ChatMessage
import com.tv.app.chat.mvi.bean.funcMsg
import com.tv.app.chat.mvi.bean.modelMsg
import com.tv.app.chat.mvi.bean.systemMsg
import com.tv.app.chat.mvi.bean.userMsg
import com.tv.app.func.FuncManager
import com.tv.app.ui.suspend.ItemViewTouchListener
import com.tv.app.ui.suspend.SuspendViewModel
import com.zephyr.extension.mvi.MVIViewModel
import com.zephyr.extension.widget.toast
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.log.logI
import com.zephyr.net.toPrettyJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

var windowListener: ItemViewTouchListener.OnTouchEventListener? = null

class ChatViewModel(
    private val generativeModel: GenerativeModel
) : MVIViewModel<ChatIntent, ChatState, ChatEffect>(), ItemViewTouchListener.OnTouchEventListener {
    private val chatManager: ChatManager by lazy { ChatManager(generativeModel) }
    private val _uiState: MutableStateFlow<ChatState> = MutableStateFlow(initUiState())
    val uiState: StateFlow<ChatState> = _uiState.asStateFlow()

    init {
        windowListener = this

        observeState {
            viewModelScope.launch {
                map { it.messages }.collect { list ->
                    val last = list.last()
                    SuspendViewModel.suspendText.value = when (last.role) {
                        Role.USER -> "正在生成"
                        Role.MODEL -> last.text.take(4) + "..."
                        Role.SYSTEM -> "未开始聊天"
                        Role.FUNC -> "正在生成"
                    }
                }
            }
        }
    }

    override fun onClick() {
        "已发送".toast()
        sendIntent(ChatIntent.Chat("[application-reminding]:call functions if needed"))
    }

    override fun onDrag() {
    }

    override fun onLongPress() {
    }

    override fun onCleared() {
        windowListener = null
        super.onCleared()
    }

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
        if (chatManager.isActive()) {
            sendEffect(ChatEffect.Generating)
            return@launch
        }
        sendEffect(ChatEffect.ChatSent)

        logI(TAG, "user:\n$text")
        val userMessage = userMsg(text, false)
        updateState { modifyList { add(userMessage) } }

        val modelMsg = modelMsg("", true)
        updateState { modifyList { add(modelMsg) } }

        try {
            processResponse(chatManager.sendMsg(userContent { text(text) }), modelMsg)
        } catch (t: Throwable) {
            t.logE(TAG)
            updateState {
                modifyList {
                    if (last().role == Role.USER)
                        add(modelMsg("出错了，请重试"))
                    else
                        last().text += "出错了，请重试"
                }
            }
        }
    }

    /**
     * 处理非流式对话响应
     */
    private suspend fun processResponse(
        response: GenerateContentResponse,
        modelMsg: ChatMessage
    ) {
        val responseText = response.text ?: ""
        val funcCalls = response.functionCalls.map { Pair(it.name, it.args) }

        updateState {
            modifyMsg(modelMsg.id) { copy(text = responseText, isPending = false) }
        }

        logI(TAG, "llm:\n$responseText")
        if (funcCalls.isNotEmpty()) {
            logI(TAG, "tools:\nabout to call ${funcCalls.size} functions")
            handleFunctionCalls(funcCalls)
        } else {
            logI(TAG, "tools:\nno function was called")
        }
    }

    /**
     * 调用本地函数，统一将结果发送给大模型
     */
    private suspend fun handleFunctionCalls(funcCalls: List<Pair<String, Map<String, String?>>>) {
        val jsons = StringBuilder()
        val results = mutableMapOf<String, JSONObject>()
        withContext(Dispatchers.IO) {
            funcCalls.forEach { (name, args) ->
                val r = FuncManager.executeFunction(name, args)
                jsons.append("$name: ${r.toPrettyJson()}\n")
                results[name] = r
            }
        }

        updateState {
            modifyList { add(funcMsg(jsons.toString(), false)) }
        }

        logI(TAG, "tools:\nhandled ${results.size} functions\n$jsons")

        val responseMsg = modelMsg("", true)
        updateState {
            modifyList { add(responseMsg) }
        }

        val funcResponse = try {
            chatManager.sendMsg(
                funcContent {
                    funcCalls.forEach { pair ->
                        val name = pair.first
                        results[name]?.let {
                            part(FunctionResponsePart(name, it))
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            t.logE(TAG)
            updateState {
                modifyMsg(responseMsg.id) { copy(text = "出错了，请重试") }
            }
            return
        }

        val funcResponseText = funcResponse.text ?: ""
        val newFuncCalls = funcResponse.functionCalls.map { Pair(it.name, it.args) }

        updateState {
            modifyMsg(responseMsg.id) { copy(text = funcResponseText, isPending = false) }
        }

        if (funcResponseText.isNotEmpty()) {
            logI(TAG, "llm:\n$funcResponseText")
        }

        if (newFuncCalls.isNotEmpty()) {
            handleFunctionCalls(newFuncCalls)
        }
    }
}
//package com.tv.app.chat
//
//import androidx.lifecycle.viewModelScope
//import com.google.ai.client.generativeai.GenerativeModel
//import com.google.ai.client.generativeai.type.FunctionResponsePart
//import com.google.ai.client.generativeai.type.GenerateContentResponse
//import com.tv.app.SYSTEM_PROMPT
//import com.tv.app.chat.mvi.ChatEffect
//import com.tv.app.chat.mvi.ChatIntent
//import com.tv.app.chat.mvi.ChatState
//import com.tv.app.chat.mvi.bean.ChatMessage
//import com.tv.app.chat.mvi.bean.funcMsg
//import com.tv.app.chat.mvi.bean.modelMsg
//import com.tv.app.chat.mvi.bean.systemMsg
//import com.tv.app.chat.mvi.bean.userMsg
//import com.tv.app.func.FuncManager
//import com.tv.app.ui.suspend.ItemViewTouchListener
//import com.zephyr.extension.mvi.MVIViewModel
//import com.zephyr.extension.widget.toast
//import com.zephyr.global_values.TAG
//import com.zephyr.log.logE
//import com.zephyr.log.logI
//import com.zephyr.net.toPrettyJson
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.json.JSONObject
//
//var windowListener: ItemViewTouchListener.OnTouchEventListener? = null
//
//class ChatViewModel(
//    private val generativeModel: GenerativeModel
//) : MVIViewModel<ChatIntent, ChatState, ChatEffect>(), ItemViewTouchListener.OnTouchEventListener {
//    private val chatManager: ChatManager by lazy { ChatManager(generativeModel) }
//    private val _uiState: MutableStateFlow<ChatState> = MutableStateFlow(initUiState())
//    val uiState: StateFlow<ChatState> = _uiState.asStateFlow()
//
//    init {
//        windowListener = this
//    }
//
//    override fun onClick() {
//        "已发送".toast()
//        sendIntent(ChatIntent.Chat("[application-reminding]:call functions if needed"))
//    }
//
//    override fun onDrag() {
//    }
//
//    override fun onLongPress() {
//    }
//
//    override fun onCleared() {
//        windowListener = null
//        super.onCleared()
//    }
//
//    override fun handleIntent(intent: ChatIntent) {
//        when (intent) {
//            is ChatIntent.Chat -> chat(intent.text)
//            ChatIntent.ResetChat -> resetChat()
//        }
//    }
//
//    override fun initUiState(): ChatState = ChatState(
//        listOf(
//            systemMsg(SYSTEM_PROMPT)
//        )
//    )
//
//    private fun resetChat() {
//        chatManager.resetChat()
//    }
//
//    private fun chat(text: String) = viewModelScope.launch(Dispatchers.IO) {
//        if (chatManager.isActive()) {
//            sendEffect(ChatEffect.Generating)
//            return@launch
//        }
//        sendEffect(ChatEffect.ChatSent)
//
//        logI(TAG, "user:\n$text")
//        val userMessage = userMsg(text, false)
//        updateState { modifyList { add(userMessage) } }
//
//        val flow = chatManager.sendMsgStream(userContent { text(text) })
//
//        val modelMsg = modelMsg("", true)
//        updateState { modifyList { add(modelMsg) } }
//
//        processResponseFlow(flow, modelMsg)
//    }
//
//    /**
//     * 处理对话流
//     */
//    private suspend fun processResponseFlow(
//        flow: Flow<GenerateContentResponse>,
//        modelMsg: ChatMessage
//    ) {
//        var responseText = ""
//        val funcCalls = mutableListOf<Pair<String, Map<String, String?>>>()
//
//        flow.setCatchEvent(modelMsg).collect { chunk ->
//            responseText += chunk.text
//            updateState {
//                modifyMsg(modelMsg.id) { copy(text = responseText) }
//            }
//            funcCalls.addAll(chunk.functionCalls.map { Pair(it.name, it.args) })
//        }
//
//        updateState {
//            modifyMsg(modelMsg.id) { copy(isPending = false) }
//        }
//
//        logI(TAG, "llm:\n$responseText")
//        if (funcCalls.isNotEmpty()) {
//            logE(TAG, "about to call ${funcCalls.size} functions")
//            handleFunctionCalls(funcCalls)
//        }
//    }
//
//    /**
//     * 调用本地函数，统一将结果发送给大模型
//     */
//    private suspend fun handleFunctionCalls(funcCalls: List<Pair<String, Map<String, String?>>>) {
//        val jsons = StringBuilder()
//        val results = mutableMapOf<String, JSONObject>()
//        withContext(Dispatchers.IO) {
//            funcCalls.forEach { (name, args) ->
//                val r = FuncManager.executeFunction(name, args)
//                jsons.append(name + ": " + r.toPrettyJson() + "\n")
//                results[name] = r
//            }
//        }
//
//        updateState {
//            modifyList { add(funcMsg(jsons.toString(), false)) }
//        }
//
//        logI(TAG, "tools:\nhandled ${results.size} functions\n$jsons")
//
//        val funcResponse = chatManager.sendMsgStream(
//            funcContent {
//                funcCalls.forEach { pair ->
//                    val name = pair.first
////                    val args = pair.second
//
//                    results[name]?.let {
////                        part(FunctionCallPart(name, args))
//                        part(FunctionResponsePart(name, it))
//                    }
//                }
//            })
//
//        val responseMsg = modelMsg("", true)
//        updateState {
//            modifyList { add(responseMsg) }
//        }
//
//        var funcResponseText = ""
//        val newFunCalls = mutableListOf<Pair<String, Map<String, String?>>>()
//
//        funcResponse.setCatchEvent(responseMsg).collect { chunk ->
//            funcResponseText += chunk.text ?: ""
//            updateState {
//                modifyMsg(responseMsg.id) { copy(text = funcResponseText) }
//            }
//            newFunCalls.addAll(chunk.functionCalls.map {
//                Pair(it.name, it.args)
//            })
//        }
//
//        updateState {
//            modifyMsg(responseMsg.id) { copy(isPending = false) }
//        }
//
//        if (funcResponseText.isNotEmpty()) {
//            logI(TAG, "llm:\n$funcResponseText")
//        }
//
//        if (newFunCalls.isNotEmpty()) {
//            handleFunctionCalls(newFunCalls)
//        }
//    }
//
//    /**
//     * 捕捉流中的异常，并更新 ui
//     */
//    private fun <T> Flow<T>.setCatchEvent(bindMsg: ChatMessage): Flow<T> {
//        return catch { e ->
//            logE(TAG, chatManager.getHistory().toPrettyJson())
//            e.logE(TAG)
//            updateState {
//                modifyMsg(bindMsg.id) { copy(text = "$text\n出错了，请重试") }
//            }
//            sendEffect(ChatEffect.Error(e))
//        }
//    }
//}