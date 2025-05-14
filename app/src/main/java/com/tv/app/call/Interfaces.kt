package com.tv.app.call

import com.tv.app.call.beans.ParsedResult
import okhttp3.WebSocketListener
import okio.ByteString

// WebSocket 管理接口
interface IWebSocketManager {
    var webSocketListener: WebSocketListener?

    fun setOnEventListener(l: OnEventListener?)

    fun connect(apiKey: String)
    fun sendMessage(message: String)
    fun sendMessage(content: WebSocketManager.ClientContentMessage)
    fun close()

    fun interface OnEventListener {
        fun onEvent(event: Event)
    }

    sealed class Event {
        data object Opened : Event()

        data class Message(val text: String) : Event()

        data object Closing : Event()

        data object Closed : Event()

        data class Failure(val t: Throwable) : Event()
    }
}

// 音频播放接口
interface IAudioPlayer {
    var sampleRate: Int

    fun initialize(sampleRate: Int)
    fun playAudio(pcmData: ByteArray)
    fun release()
}

// 消息解析接口
interface IMessageParser {
    fun parseTextMessage(text: String): ParsedResult?
    fun parseBinaryMessage(bytes: ByteString): ParsedResult?
    fun isSetupComplete(): Boolean
}