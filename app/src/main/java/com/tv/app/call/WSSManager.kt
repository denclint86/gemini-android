package com.tv.app.call

import com.google.gson.annotations.SerializedName
import com.tv.app.call.beans.BidiGenerateContentSetup
import com.tv.app.call.beans.GenerationConfig1
import com.tv.app.call.beans.ParsedResult
import com.tv.app.call.beans.SetupConfig1
import com.tv.app.call.beans.SpeechConfig
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import com.zephyr.net.toPrettyJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString


class WSSManager(
    private val scope: CoroutineScope,
    private val messageParser: IMessageParser,
    private val audioPlayer: IAudioPlayer
) : IWebSocketManager {
    private var webSocket: WebSocket? = null
    private val client by lazy {
        OkHttpClient()
    }

    private var listener: IWebSocketManager.OnEventListener? = null

    private fun getUrl(apiKey: String): String {
        val apiVersion = "v1beta"
        return "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.${apiVersion}.GenerativeService.BidiGenerateContent?key=$apiKey"
    }

    override fun setOnEventListener(l: IWebSocketManager.OnEventListener?) {
        listener = l
    }

    override fun connect(apiKey: String) {
        scope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(getUrl(apiKey))
                .build()
            webSocket = client.newWebSocket(request, webSocketListener!!)
        }
    }

    override fun sendMessage(content: ClientContentMessage) {
        scope.launch {
            webSocket?.send(content.toJson()) ?: logE(TAG, "WebSocket 未连接")
        }
    }

    override fun sendMessage(message: String) {
        scope.launch {
            webSocket?.send(message) ?: logE(TAG, "WebSocket 未连接")
        }
    }

    @Synchronized
    override fun close() {
        webSocket?.close(1000, "正常关闭")
        webSocket = null
        audioPlayer.release()
    }

    private fun sendSetupMessage() {
        val setupMessage = BidiGenerateContentSetup(
            setup = SetupConfig1(
                model = "models/gemini-2.0-flash-exp",
                generationConfig = GenerationConfig1(
                    responseModalities = listOf("AUDIO"),
                    speechConfig = SpeechConfig("en-US")
                )
            )
        )
        logE(TAG, setupMessage.toPrettyJson())
        sendMessage(setupMessage.toJson())
    }

    override var webSocketListener: WebSocketListener? = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            logE(TAG, "WebSocket 连接已打开")

            this@WSSManager.webSocket = webSocket
            sendSetupMessage()
            scope.launch { audioPlayer.initialize(24000) }

            listener?.onEvent(IWebSocketManager.Event.Opened)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            logE(TAG, "接收到文本消息: $text")

            when (val result = messageParser.parseTextMessage(text)) {
                is ParsedResult.Audio -> {
                    if (result.sampleRate != audioPlayer.sampleRate) {
                        audioPlayer.initialize(result.sampleRate)
                    }
                    audioPlayer.playAudio(result.pcmData)
                }

                ParsedResult.SetupCompleted ->
                    listener?.onEvent(IWebSocketManager.Event.SetupCompleted)

                null ->
                    logE(TAG, "wss 解析出空数据")
            }

            listener?.onEvent(IWebSocketManager.Event.Message(text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            logE(TAG, "接收到字节流数据")
            onMessage(webSocket, bytes.utf8())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            logE(TAG, "WebSocket 连接正在关闭: $code $reason")

            listener?.onEvent(IWebSocketManager.Event.Closing)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            logE(TAG, "WebSocket 连接已关闭: $code $reason")

            audioPlayer.release()
            listener?.onEvent(IWebSocketManager.Event.Closed)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.logE(TAG)

            audioPlayer.release()
            listener?.onEvent(IWebSocketManager.Event.Failure(t))
        }
    }


    data class ClientContentMessage(
        @SerializedName("client_content") val clientContent: ClientContent
    ) {
        data class ClientContent(
            val turns: List<Turn>,
            @SerializedName("turn_complete") val turnComplete: Boolean
        ) {
            data class Turn(
                val role: String,
                val parts: List<Part>
            ) {
                data class Part(
                    val text: String
                )
            }
        }
    }
}