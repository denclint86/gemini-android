package com.tv.app.chat

import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.FunctionCallPart
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.Part
import com.tv.app.api.live.ClientContentMessage
import com.tv.app.api.live.LiveChatClient
import com.tv.app.api.live.interfaces.IWebSocketManager
import com.tv.app.chat.beans.ChatMessage
import com.tv.app.chat.beans.modelMsg
import com.tv.app.chat.beans.systemMsg
import com.tv.app.chat.beans.systemMsgList
import com.tv.app.chat.interfaces.IChatManager
import com.tv.app.chat.interfaces.IFuncHandler
import com.tv.app.chat.interfaces.IResponseHandler
import com.tv.app.chat.models.ChatHelper
import com.tv.app.chat.models.ChatManager
import com.tv.app.chat.models.FuncHandler
import com.tv.app.chat.models.ResponseHandler
import com.tv.app.chat.models.StateUpdater
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.chat.mvi.ChatState
import com.tv.app.chat.mvi.observe
import com.tv.app.view.suspendview.SuspendViewService
import com.tv.app.view.suspendview.interfaces.SuspendViewEventCallback
import com.tv.settings.getSetting
import com.tv.settings.intances.Live
import com.tv.settings.intances.SleepTime
import com.tv.settings.intances.Stream
import com.tv.settings.intances.Tools
import com.tv.utils.Role
import com.tv.utils.funcContent
import com.tv.utils.toUIString
import com.tv.utils.userContent
import com.zephyr.extension.mvi.MVIViewModel
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.net.SocketException


class ChatViewModel : MVIViewModel<ChatIntent, ChatState, ChatEffect>() {
    companion object {
        var suspendViewCallback: SuspendViewEventCallback? = null
    }

    private val client = LiveChatClient(viewModelScope)

    private val chatManager: IChatManager<Content, GenerateContentResponse>
        get() = ChatManager

    private val funcHandler: IFuncHandler<FunctionCallPart> = FuncHandler()

    private val responseHandler: IResponseHandler<GenerateContentResponse> = ResponseHandler()

    private val stateUpdater: StateUpdater = StateUpdater()
    private var liveHistory: MutableList<ClientContentMessage.ClientContent.Turn> = mutableListOf()

    val stateValue: ChatState
        get() = uiStateFlow.value


    private var job: Job? = null


    init {
//        windowListener = this
        observe(viewModelScope, { it.messages }) { list ->
            SuspendViewService.update(list.last())
        }

        stateUpdater.setUpdateStateMethod(::updateState)

//        sendIntent(ChatIntent.Chat("屏幕上面显示什么内容?"))
    }


    // mvi - {
    override fun initUiState(): ChatState = runBlocking {
        ChatState( // testMsgList,
            systemMsgList
        )
    }

    override fun handleIntent(intent: ChatIntent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (intent) {
                is ChatIntent.Chat -> {
                    if (getSetting<Live>()?.value(true)!!) {
                        liveChat(intent.text)
                    } else {
                        val userContent = userContent {
                            text(intent.text)
                        }
                        chat(userContent, getSetting<Stream>()?.value(true)!!)
                    }

                }

                ChatIntent.ResetChat -> {
                    // live
                    liveHistory.clear()
                    client.close()
                    client.setOnEventListener(null)

                    chatManager.resetChat()
                    stateUpdater.resetState()
                }

                ChatIntent.ReloadChat ->
                    stateUpdater.updateAt(0, systemMsg)

                ChatIntent.LogHistory -> {
                    val stringBuilder = StringBuilder()
                    chatManager.history.forEach { content ->
                        stringBuilder.append("[${content.role}]: ${content.parts.toUIString()}")
                        stringBuilder.append("\n")
                    }
                    logE(TAG, stringBuilder.toString())
                }
            }
        }
    }
    // } - mvi


    // live - {
    private fun liveChat(message: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            val modelMsg = modelMsg("", true)
            val helper = ChatHelper.bindTo(stateUpdater, modelMsg)

            client.setLiveListener(message, helper)

            if (!client.isAlive && !client.isSetupComplete) {
                logE(TAG, "重建 web socket")
                client.connect()
            } else {
                logE(TAG, "复用 web socket")
                sendLiveMessage(message)
            }

            sendEffect(ChatEffect.ChatSent(true))
            val userContent = userContent { text(message) }
            val userMessage = ChatMessage.fromContent(userContent)
            stateUpdater.addMessage(userMessage)

            stateUpdater.addMessage(modelMsg)
        }
    }

    private fun LiveChatClient.setLiveListener(message: String, helper: ChatHelper) {
        setOnEventListener { e ->
            when (e) {
                is IWebSocketManager.Event.Message -> {
                    helper.append(e.text)
                    helper.update {
                        text += e.text
                    }
                }

                IWebSocketManager.Event.SetupCompleted ->
                    sendLiveMessage(message)

                is IWebSocketManager.Event.Down -> {
                    e.t?.let { t ->
                        if (t !is SocketException)
                            helper.handleError(t)
                    } ?: run { sendEffect(ChatEffect.Done) }
                    client.close()
                    client.setOnEventListener(null)
                }

                IWebSocketManager.Event.TurnComplete -> {
                    helper.update { isPending = false }
                    liveHistory.add(
                        ClientContentMessage.ClientContent.Turn(
                            "model",
                            helper.string
                        )
                    )
                    sendEffect(ChatEffect.Done)
                    client.setOnEventListener(null)
                }

                else ->
                    logE(TAG, e::class.java.simpleName)
            }
        }
    }

    private fun sendLiveMessage(message: String) {
        val turn = ClientContentMessage.ClientContent.Turn("user", message)

        liveHistory.add(turn)
        client.sendClientContent(liveHistory.toList())
    }
    // } - live


    // genai-chat - {
    private fun chat(content: Content, stream: Boolean, sendImageAtTheEnd: Boolean = false) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            if (chatManager.isActive) {
                sendEffect(ChatEffect.Generating)
                return@launch
            } else if (content.role != Role.FUNC.str) {
                sendEffect(ChatEffect.ChatSent(true))
            }

            val contentMessage = ChatMessage.fromContent(content)

            stateUpdater.addMessage(contentMessage)

            val modelMsg = modelMsg("", true)
            val helper = ChatHelper.bindTo(stateUpdater, modelMsg)

            stateUpdater.addMessage(modelMsg)

            helper.apply {
                try {
                    chatInternal(stream, content)
                } catch (t: Throwable) {
                    handleError(t)
                    sendEffect(ChatEffect.Done)
                    return@launch
                }

                update {
                    text = toUIString(string, calls)
                    isPending = false
                }
            }

            if (sendImageAtTheEnd) {
                val capture = SuspendViewService.binder?.captureManager?.capture() ?: return@launch
                val imageContent = userContent {
                    text("[这是客户端发送的]")
                    image(capture)
                }
                chat(imageContent, stream)
            } else {
                val funcResult = funcHandler.handleParts(helper.calls)
                handleFunctionCalls(stream, funcResult)
            }
        }
    }

    private suspend fun handleFunctionCalls(stream: Boolean, funcResult: Map<String, JSONObject>) {
        val delay = getSetting<SleepTime>()?.value(true)!!
        delay(delay)

        var sendImageAtTheEnd = false
        val parts = mutableListOf<Part>()
        funcResult.forEach { (name, jsonObj) ->
//            if (name == ScreenContentModel.name) {
//                sendImageAtTheEnd = true
//            }

            parts.add(FunctionResponsePart(name, jsonObj))
        }

        val funcContent = funcContent {
            this.parts.addAll(parts)
        }

        if (funcResult.isNotEmpty() && getSetting<Tools>()?.isEnabled() != false)
            chat(funcContent, stream, sendImageAtTheEnd)
        else
            sendEffect(ChatEffect.Done)
    }

    private suspend fun ChatHelper.chatInternal(
        stream: Boolean,
        content: Content
    ) {
        if (stream) {
            sendStream(content)
        } else {
            send(content)
        }
    }

    private suspend fun ChatHelper.sendStream(content: Content) {
        val response = chatManager.sendMsgStream(content)

        responseHandler.handle(response)
            .collect { result ->
                update {
                    text += result.text
                }
                append(result.text)
                addAll(result.raw.functionCalls)
            }
    }

    private suspend fun ChatHelper.send(content: Content) {
        val response = chatManager.sendMsg(content)
        append(response.text)
        addAll(response.functionCalls)
    }
    // } - genai-chat
}