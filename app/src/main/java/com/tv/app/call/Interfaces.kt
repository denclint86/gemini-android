package com.tv.app.call

import okio.ByteString

// WebSocket 管理接口
interface IWebSocketManager {
    fun connect(apiKey: String)
    fun sendMessage(message: String)
    fun close()
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
    fun parseTextMessage(text: String): Boolean
    fun parseBinaryMessage(bytes: ByteString): AudioData?
    fun isSetupComplete(): Boolean
}