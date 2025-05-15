package com.tv.app.chat.models

import com.tv.app.chat.beans.ChatMessage
import com.tv.app.chat.beans.systemMsgList
import com.tv.app.chat.interfaces.IStateUpdater
import com.tv.app.chat.mvi.ChatState
import com.tv.utils.Role
import java.util.UUID
import kotlin.reflect.KFunction1

class StateUpdater : IStateUpdater<ChatMessage, ChatState, StateUpdater.Builder> {
    private var method: (KFunction1<ChatState.() -> ChatState, Unit>)? = null

    override fun setUpdateStateMethod(method: KFunction1<ChatState.() -> ChatState, Unit>) {
        this.method = method
    }

    fun updateAt(index: Int, newMsg: ChatMessage) {
        method?.invoke {
            modifyList { set(index, newMsg) }
        }
    }

    override fun addMessage(message: ChatMessage) {
        method?.invoke {
            modifyList { add(message) }
        }
    }

    override fun updateMessage(
        which: ChatMessage.() -> Boolean,
        update: Builder.() -> Unit
    ) {
        method?.invoke {
            modifyMsg(which) {
                val builder = Builder(this)
                builder.update()
                builder.msg
            }
        }
    }

    override fun resetState() {
        method?.invoke {
            ChatState(systemMsgList)
        }
    }

    class Builder(msg: ChatMessage) {
        @JvmField
        var id: UUID = msg.id

        @JvmField
        var text: String = msg.text

        @JvmField
        var role: Role = msg.role

        @JvmField
        var isPending: Boolean = msg.isPending

        val msg: ChatMessage
            get() = ChatMessage(id, text, role, isPending)
    }
}