package com.tv.app.chat.models;

import com.google.ai.client.generativeai.type.FunctionCallPart
import com.tv.app.chat.beans.ChatMessage
import com.zephyr.global_values.TAG
import com.zephyr.log.logE

class ChatHelper private constructor(
    private val updater: StateUpdater,
    private val msg: ChatMessage
) {

    private val stringBuilder = StringBuilder()
    private val list = mutableListOf<FunctionCallPart>()

    companion object {
        private const val ERROR_UI_MSG = "出错了, 请重试"

        fun bindTo(updater: StateUpdater, msg: ChatMessage): ChatHelper {
            return ChatHelper(updater, msg)
        }
    }

    val string: String
        get() = stringBuilder.toString()
    val calls: List<FunctionCallPart>
        get() = list.toList()

    fun update(update: StateUpdater.Builder. () -> Unit) {
        updater.updateMessage({ id == msg.id }, update)
    }

    fun append(text: String?) {
        stringBuilder.append(text)
    }

    fun addAll(calls: Collection<FunctionCallPart>) {
        list.addAll(calls)
    }

    fun handleError(t: Throwable) {
        t.logE(TAG)
        updater.updateMessage({ id == msg.id }) {
            text = (text + "\n" + ERROR_UI_MSG + "\n" + t.message).trimStart()
            isPending = false
        }
    }
}