package com.tv.app.chat

import com.tv.app.chat.mvi.ChatState
import com.tv.app.chat.mvi.bean.ChatMessage
import java.util.UUID
import kotlin.reflect.KFunction1

interface IStateUpdater<T> {
    fun setUpdateStateMethod(method: KFunction1<T.() -> T, Unit>)
    fun addMessage(state: ChatState, message: ChatMessage)
    fun updateMessage(
        state: ChatState,
        uuid: UUID,
        update: (ChatMessage) -> ChatMessage
    )

    fun resetState()
}