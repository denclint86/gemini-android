package com.tv.app.chat

import com.tv.app.chat.mvi.ChatState
import com.tv.app.chat.mvi.bean.ChatMessage
import com.tv.app.utils.getSystemPromptMsg
import java.util.UUID
import kotlin.reflect.KFunction1

class StateUpdater : IStateUpdater<ChatState> {
    private var method: (KFunction1<ChatState.() -> ChatState, Unit>)? = null

    override fun setUpdateStateMethod(method: KFunction1<ChatState.() -> ChatState, Unit>) {
        this.method = method
    }

    override fun addMessage(state: ChatState, message: ChatMessage) {
        method?.invoke {
            state.copy(messages = state.messages + message)
        }
    }

    override fun updateMessage(
        state: ChatState,
        uuid: UUID,
        update: (ChatMessage) -> ChatMessage
    ) {
        method?.invoke {
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == uuid) update(msg) else msg
                }
            )
        }
    }

    override fun resetState() {
        method?.invoke {
            ChatState(listOf(getSystemPromptMsg()))
        }
    }
}