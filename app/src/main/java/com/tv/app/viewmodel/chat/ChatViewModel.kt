package com.tv.app.viewmodel.chat

import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.Content
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
import com.tv.app.utils.ROLE
import com.tv.app.utils.funcContent
import com.tv.app.utils.getScreenAsBitmap
import com.tv.app.utils.getSystemPromptMsg
import com.tv.app.utils.logC
import com.tv.app.utils.observe
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject


var windowListener: ItemViewTouchListener.OnTouchEventListener? = null

class ChatViewModel : MVIViewModel<ChatIntent, ChatState, ChatEffect>()
//, ItemViewTouchListener.OnTouchEventListener
{
    companion object {
        private const val ERROR_UI_MSG = "出错了，请重试"
        const val CHAT_TAG = "ChatTag" // 用于过滤日志
    }

    private val chatManager: IChatManager<Content, GenerateContentResponse>
        get() = ChatManager
    private val funcHandler: IFuncHandler = FuncHandler()
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
            listOf(
                getSystemPromptMsg(),
//                ChatMessage(text = "UUUUaaaaa\nbbbbb\nccccc\nddddd", role = Role.USER),
//                ChatMessage(text = "MMMMaaaaa\nbbbbb\nccccc\nddddd", role = Role.MODEL),
//                ChatMessage(text = "FFFFaaaaa\nbbbbb\nccccc\nddddd", role = Role.FUNC),
//                ChatMessage(text = "UUUUaaaaa\nbbbbb\nccccc\nddddd", role = Role.USER),
            ),
            buttonState = State.COLLAPSED
        )
    }

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.Chat -> {
                val userContent = userContent { text(intent.text) }
                chat(userContent, SettingsRepository.streamSetting.value(true)!!)
            }

            ChatIntent.ResetChat ->
                viewModelScope.launch {
                    chatManager.resetChat()
                    stateUpdater.resetState()
                }

            ChatIntent.ReloadChat ->
                stateUpdater.updateAt(0, getSystemPromptMsg())

            is ChatIntent.SaveButtonState -> updateState { copy(buttonState = buttonState) }
        }
    }

    private fun chat(content: Content, stream: Boolean) {
        viewModelScope.launch {
            if (chatManager.isActive) {
                sendEffect(ChatEffect.Generating)
                return@launch
            }
            sendEffect(ChatEffect.ChatSent(true))

            logC(content.toString())
            val contentMessage = ChatMessage(
                text = content.toUIString(),
                role = content.ROLE,
                isPending = false
            )
            stateUpdater.addMessage(contentMessage)

            val modelMsg = modelMsg("", true)
            stateUpdater.addMessage(modelMsg)

            val funcResult = hashMapOf<String, JSONObject>()
            if (stream) {
                val response = chatManager.sendMsgStream(content)

                val textBuilder = StringBuilder()
                val functionCalls = mutableListOf<Part>()
                responseHandler.handle(response)
                    .catch { t ->
                        stateUpdater.updateMessage({ id == modelMsg.id }) {
                            text = (text + "\n" + ERROR_UI_MSG + "\n" + t.message).trim()
                            isPending = false
                        }
                    }
                    .collect { result ->
                        stateUpdater.updateMessage({ id == modelMsg.id }) {
                            text += result.text
                        }

                        textBuilder.append(result.text)
                        functionCalls.addAll(result.raw.functionCalls)

                        val f = funcHandler.handle(result.functionCalls)
                        funcResult.putAll(f)
                    }

                stateUpdater.updateMessage({ id == modelMsg.id }) {
                    text = toUIString(textBuilder.toString(), functionCalls)
                    isPending = false
                }
            } else {
                try {
                    val response = chatManager.sendMsg(content)
                    val result = responseHandler.handle(response)

                    stateUpdater.updateMessage({ id == modelMsg.id }) {
                        text = response.toUIString()
                        isPending = false
                    }

                    val f = funcHandler.handle(result.functionCalls)
                    funcResult.putAll(f)
                } catch (t: Throwable) {
                    stateUpdater.updateMessage({ id == modelMsg.id }) {
                        text = (text + "\n" + ERROR_UI_MSG + "\n" + t.message).trim()
                        isPending = false
                    }
                }
            }

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
    }
}