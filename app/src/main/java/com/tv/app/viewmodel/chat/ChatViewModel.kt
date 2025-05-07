package com.tv.app.viewmodel.chat

import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.FunctionCallPart
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.Part
import com.tv.app.model.ChatManager
import com.tv.app.model.FuncHandler
import com.tv.app.model.ResponseHandler
import com.tv.app.model.SettingsRepository
import com.tv.app.model.interfaces.IChatManager
import com.tv.app.model.interfaces.IFuncHandler
import com.tv.app.model.interfaces.IResponseHandler
import com.tv.app.old.func.models.VisibleViewsModel
import com.tv.app.utils.funcContent
import com.tv.app.utils.getScreenAsBitmap
import com.tv.app.utils.observe
import com.tv.app.utils.systemMsg
import com.tv.app.utils.systemMsgList
import com.tv.app.utils.toUIString
import com.tv.app.utils.userContent
import com.tv.app.view.ui.suspend.ItemViewTouchListener
import com.tv.app.view.ui.suspend.SuspendLiveDataManager
import com.tv.app.viewmodel.StateUpdater
import com.tv.app.viewmodel.chat.mvi.ChatEffect
import com.tv.app.viewmodel.chat.mvi.ChatIntent
import com.tv.app.viewmodel.chat.mvi.ChatState
import com.tv.app.viewmodel.chat.mvi.bean.ChatMessage
import com.tv.app.viewmodel.chat.mvi.bean.modelMsg
import com.zephyr.extension.mvi.MVIViewModel
import com.zephyr.scaling_layout.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject


var windowListener: ItemViewTouchListener.OnTouchEventListener? = null

class ChatViewModel : MVIViewModel<ChatIntent, ChatState, ChatEffect>()
//, ItemViewTouchListener.OnTouchEventListener
{
    companion object {
        const val ERROR_UI_MSG = "出错了，请重试"
        const val CHAT_TAG = "ChatTag" // 用于过滤日志
    }

    private val chatManager: IChatManager<Content, GenerateContentResponse>
        get() = ChatManager
    private val funcHandler: IFuncHandler<FunctionCallPart> = FuncHandler()
    private val responseHandler: IResponseHandler<GenerateContentResponse> = ResponseHandler()
    private val stateUpdater: StateUpdater = StateUpdater()

    val stateValue: ChatState
        get() = uiStateFlow.value

    init {
//        windowListener = this
        observe(viewModelScope, { it.messages }) { list ->
            SuspendLiveDataManager.update(list.last())
        }

        stateUpdater.setUpdateStateMethod(::updateState)
    }

    override fun initUiState(): ChatState = runBlocking {
        ChatState(
// testMsgList,
            systemMsgList,
            buttonState = State.COLLAPSED
        )
    }

    override fun handleIntent(intent: ChatIntent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (intent) {
                is ChatIntent.Chat -> {
                    val userContent = userContent { text(intent.text) }
                    chat(userContent, SettingsRepository.streamSetting.value(true)!!)
                }

                ChatIntent.ResetChat -> {
                    chatManager.resetChat()
                    stateUpdater.resetState()
                }

                ChatIntent.ReloadChat ->
                    stateUpdater.updateAt(0, systemMsg)

                is ChatIntent.SaveButtonState -> updateState { copy(buttonState = buttonState) }
            }
        }
    }

    private suspend fun chat(content: Content, stream: Boolean) {
        if (chatManager.isActive) {
            sendEffect(ChatEffect.Generating)
            return
        } else {
            sendEffect(ChatEffect.ChatSent(true))
        }

        val contentMessage = ChatMessage.fromContent(content)

        stateUpdater.addMessage(contentMessage)

        val modelMsg = modelMsg("", true)
        val helper = ChatHelper.bindTo(stateUpdater, modelMsg)

        stateUpdater.addMessage(modelMsg)

        helper.chatInternal(stream, content)

        helper.apply {
            update {
                text = toUIString(string, calls)
                isPending = false
            }
        }

        val funcResult = funcHandler.handleParts(helper.calls)
        handleFunctionCalls(stream, funcResult)
    }

    private suspend fun handleFunctionCalls(stream: Boolean, funcResult: Map<String, JSONObject>) {
        val parts = mutableListOf<Part>()
        funcResult.forEach { (name, jsonObj) ->
            val bitmap = if (name == VisibleViewsModel.name)
                getScreenAsBitmap()
            else
                null
            if (bitmap != null)
                parts.add(ImagePart(bitmap))

            parts.add(FunctionResponsePart(name, jsonObj))
        }

        val funcContent = funcContent {
            this.parts.addAll(parts)
        }

        if (funcResult.isNotEmpty() && SettingsRepository.toolsSetting.isEnabled())
            chat(funcContent, stream)
    }

    private suspend fun ChatHelper.chatInternal(
        stream: Boolean,
        content: Content
    ) = try {
        if (stream) {
            sendStream(content)
        } else {
            send(content)
        }
    } catch (t: Throwable) {
        handleError(t)
    }

    private suspend fun ChatHelper.sendStream(content: Content) {
        val response = chatManager.sendMsgStream(content)

        responseHandler.handle(response)
            .catch { t ->
                handleError(t)
            }
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
}