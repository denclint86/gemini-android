package com.tv.app.api.live.interfaces

import com.tv.app.api.live.ClientContentMessage
import okhttp3.WebSocketListener

// WebSocket 管理接口
interface IWebSocketManager {
    var webSocketListener: WebSocketListener?
    val isAlive: Boolean

    fun setOnEventListener(l: OnEventListener?)

    fun connect()

    /**
     * 用于发送原数据
     */
    fun sendMessage(raw: String)
    fun sendMessage(content: ClientContentMessage)
    fun close()

    fun interface OnEventListener {
        fun onEvent(event: Event)
    }

    sealed class Event {
        data object Opened : Event()

        data object SetupCompleted : Event()

        data class Message(val text: String) : Event()

        data object TurnComplete : Event()

        data class Down(val t: Throwable?) : Event()
    }
}