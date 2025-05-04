package com.tv.app.chat

import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.Part
import com.tv.app.chat.mvi.ChatEffect
import com.tv.app.chat.mvi.ChatIntent
import com.tv.app.chat.mvi.ChatState
import com.tv.app.chat.mvi.bean.ChatMessage
import com.tv.app.chat.mvi.bean.modelMsg
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

    private val chatManager: IChatManager
        get() = ChatManager
    private val funcHandler: IFuncHandler = FuncHandler()
    private val responseHandler: IResponseHandler = ResponseHandler()
    private val stateUpdater: StateUpdater = StateUpdater()


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
            is ChatIntent.Chat -> {
                val userContent = userContent { text(intent.text) }
                chat(userContent, SettingsRepository.streamSetting.value ?: Default.STREAM)
            }

            ChatIntent.ResetChat ->
                viewModelScope.launch {
                    chatManager.resetChat()
                    stateUpdater.resetState()
                }

            ChatIntent.ReloadChat ->
                stateUpdater.updateAt(0, getSystemPromptMsg())
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
                responseHandler.handle(response)
                    .catch { t ->
                        stateUpdater.updateMessage({ id == modelMsg.id }) {
                            this.text = (this.text + "\n" + ERROR_UI_MSG + "\n" + t.message).trim()
                            isPending = false
                        }
                    }
                    .collect { result ->
                        stateUpdater.updateMessage({ id == modelMsg.id }) {
                            text += result.text
                        }

                        val f = funcHandler.handle(result.functionCalls)
                        funcResult.putAll(f)
                    }
                stateUpdater.updateMessage({ id == modelMsg.id }) {
                    isPending = false
                }
            } else {
                val response = chatManager.sendMsg(content)
                val result = responseHandler.handle(response)

                stateUpdater.updateMessage({ id == modelMsg.id }) {
                    text = result.text
                    isPending = false
                }

                val f = funcHandler.handle(result.functionCalls)
                funcResult.putAll(f)
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

            if (funcResult.isNotEmpty())
                chat(funcContent, stream)
        }
    }
}