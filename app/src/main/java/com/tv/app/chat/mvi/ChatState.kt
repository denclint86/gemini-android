package com.tv.app.chat.mvi

import com.tv.app.chat.mvi.bean.ChatMessage
import java.util.UUID

data class ChatState(
    private val messages: MutableList<ChatMessage> = mutableListOf(),
    var listLastUpdatedTs: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
) {
    fun modifyMsg(uuid: UUID, block: ChatMessage.() -> ChatMessage): ChatState {
        val index = messages.indexOfFirst { it.id == uuid }
        if (index == -1)
            return this
        else
            messages[index] = (messages[index].block())
        return newThis()
    }

    fun modifyList(block: MutableList<ChatMessage>.() -> Unit): ChatState {
        messages.block()
        return newThis()
    }

    fun setLastPending(): ChatState {
        if (messages.isNotEmpty()) {
            messages[messages.lastIndex] = messages.last().copy(isPending = false)
            return newThis()
        } else {
            return this
        }
    }

    private fun newThis() = copy(listLastUpdatedTs = System.currentTimeMillis())
}