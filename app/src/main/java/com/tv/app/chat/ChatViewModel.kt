package com.tv.app.chat

import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.chat.mvi.ChatState
import com.tv.app.chat.mvi.bean.ChatMessage
import com.tv.app.chat.mvi.bean.funcMsg
import com.tv.app.chat.mvi.bean.modelMsg
import com.tv.app.chat.mvi.bean.userMsg
import com.tv.app.func.FuncManager
import com.tv.app.func.models.VisibleViewsModel
import com.tv.app.model.ChatManager
import com.tv.app.model.FuncHandler
import com.tv.app.model.IChatManager
import com.tv.app.model.IFuncHandler
import com.tv.app.model.IResponseHandler
import com.tv.app.model.ResponseHandler
import com.tv.app.settings.SettingsRepository
import com.tv.app.settings.values.Default
import com.tv.app.ui.suspend.ItemViewTouchListener
import com.tv.app.ui.suspend.SuspendLiveDataManager
import com.tv.app.utils.getScreenAsBitmap
import com.tv.app.utils.getSystemPromptMsg
import com.tv.app.utils.logC
import com.tv.app.utils.observe
import com.zephyr.extension.mvi.MVIViewModel
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toPrettyJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject


var windowListener: ItemViewTouchListener.OnTouchEventListener? = null

class ChatViewModel : MVIViewModel<ChatIntent, ChatState, ChatEffect>()
//, ItemViewTouchListener.OnTouchEventListener
{
    companion object {
        private const val ERROR_UI_MSG = "出错了，请重试"
        const val CHAT_TAG = "ChatTag" // 用于过滤日志
    }

    private val chatManager: IChatManager
        get() = ChatManager
    private val funcHandler: IFuncHandler = FuncHandler()
    private val responseHandler: IResponseHandler = ResponseHandler()
    private val stateUpdater: IStateUpdater<ChatState> = StateUpdater()


    init {
//        windowListener = this
        observe(viewModelScope, { it.messages }) { list ->
            SuspendLiveDataManager.update(list.last())
        }

        stateUpdater.setUpdateStateMethod(::updateState)
    }

    override fun initUiState(): ChatState = runBlocking {
        ChatState(
            listOf(getSystemPromptMsg())
        )
    }

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.Chat -> chat(intent.text)

            ChatIntent.ResetChat ->
                viewModelScope.launch {
                    chatManager.resetChat()
                    updateState { initUiState() }
                }

            ChatIntent.ReloadChat -> updateState {
                modifyList {
                    set(0, getSystemPromptMsg())
                }
            }
        }
    }


    fun chat2(text: String, stream: Boolean) = viewModelScope.launch {
        if (chatManager.isActive) {
            sendEffect(ChatEffect.Generating)
            return@launch
        }
        sendEffect(ChatEffect.ChatSent(true))

        logC("user:\n$text")
        val userMessage = userMsg(text, false)
        updateState { modifyList { add(userMessage) } }

        val modelMsg = modelMsg("", true)
        updateState { modifyList { add(modelMsg) } }

        if (stream) {
            val funcResult = hashMapOf<String, JSONObject>()

            val response = chatManager.sendMsgStream(userContent { text(text) })
            responseHandler.handle(response).collect { result ->
                result.text

                funcResult.putAll(funcHandler.handle(result.functionCalls))
            }
        } else {
            val response = chatManager.sendMsg(userContent { text(text) })
            val result = responseHandler.handle(response)

            result.text

            val funcResult = funcHandler.handle(
                result.functionCalls
            )
        }
    }

    /**
     * 对话日志
     */
    private fun chat(text: String) = viewModelScope.launch(Dispatchers.IO) {
        if (chatManager.isActive) {
            sendEffect(ChatEffect.Generating)
            return@launch
        }
        sendEffect(ChatEffect.ChatSent(true))

        logC("user:\n$text")
        val userMessage = userMsg(text, false)
        updateState { modifyList { add(userMessage) } }

        val modelMsg = modelMsg("", true)
        updateState { modifyList { add(modelMsg) } }

        try {
            processResponse(chatManager.sendMsg(userContent { text(text) }), modelMsg)
        } catch (t: Throwable) {
            t.logE(TAG)
            updateState {
                modifyMsg(modelMsg.id) {
                    copy(
                        text = (this.text + "\n" + ERROR_UI_MSG + "\n" + t.message).trim(),
                        isPending = false
                    )
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

        logC("llm:\n$responseText")
        if (funcCalls.isNotEmpty()) {
            logC("tools:\nabout to call ${funcCalls.size} functions")
            handleFunctionCalls(funcCalls)
        } else {
            logC("tools:\nno function was called")
        }
    }

    /**
     * 调用本地函数，统一将结果发送给大模型
     */
    private suspend fun handleFunctionCalls(funcCalls: List<Pair<String, Map<String, String?>?>>) {
        val jsons = StringBuilder()
        val results = mutableMapOf<String, JSONObject>()
        withContext(Dispatchers.IO) {
            funcCalls.forEach { (name, args) ->
                if (args == null) return@forEach
                val r = FuncManager.executeFunction(name, args)
                jsons.append("$name($args): ${r.toPrettyJson()}\n")
                results[name] = JSONObject(r)
            }
        }

        updateState {
            modifyList { add(funcMsg(jsons.toString(), false)) }
        }

        logC("tools:\nhandled ${results.size} functions")
        logC("tools:\n$jsons", false)

        logE(TAG, "sleep")
        delay(SettingsRepository.sleepTimeSetting.value ?: Default.SLEEP_TIME)

        val responseMsg = modelMsg("", true)
        updateState {
            modifyList { add(responseMsg) }
        }

        sendEffect(ChatEffect.ChatSent(false))

        val funcResponse = try {
            chatManager.sendMsg(
                funcContent {
                    funcCalls.forEach { pair ->
                        val name = pair.first
                        val bitmap =
                            if (name == VisibleViewsModel.name)
                                runBlocking {
                                    getScreenAsBitmap()
                                } else null

                        results[name]?.let {
                            if (bitmap != null)
                                image(bitmap)
                            part(FunctionResponsePart(name, it))
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            t.logE(TAG)
            updateState {
                modifyMsg(responseMsg.id) {
                    copy(
                        text = (text + "\n" + ERROR_UI_MSG + "\n" + t.message).trim(),
                        isPending = false
                    )
                }
            }
            return
        }

        val funcResponseText = funcResponse.text ?: ""
        val newFuncCalls = funcResponse.functionCalls.map { Pair(it.name, it.args) }

        updateState {
            modifyMsg(responseMsg.id) { copy(text = funcResponseText, isPending = false) }
        }

        if (funcResponseText.isNotEmpty()) {
            logC("llm:\n$funcResponseText")
        }

        if (newFuncCalls.isNotEmpty()) {
            handleFunctionCalls(newFuncCalls)
        }
    }
}