package com.tv.app.api.live

import com.tv.app.api.live.beans.BidiGenerateContentSetup
import com.tv.app.api.live.beans.ParsedResult
import com.tv.app.api.live.beans.SetupConfig
import com.tv.app.api.live.interfaces.IAudioPlayer
import com.tv.app.api.live.interfaces.IMessageParser
import com.tv.app.api.live.interfaces.IWebSocketManager
import com.tv.app.ApiModelProvider
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

/**
 * 谷歌 genai live api 实现
 */
abstract class WebSocketManager(
    private val scope: CoroutineScope,
    private val messageParser: IMessageParser,
    private val audioPlayer: IAudioPlayer
) : IWebSocketManager {
    private var webSocket: WebSocket? = null
    private val client by lazy {
        OkHttpClient()
    }

    private var lastOnMsg = 0L
    private var timeoutJob: Job? = null

    override val isAlive
        get() = webSocket != null
    private var listener: IWebSocketManager.OnEventListener? = null

    abstract fun getSetupConfig(): SetupConfig

    private fun getUrl(apiKey: String): String {
        val apiVersion = "v1beta"
        return "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.${apiVersion}.GenerativeService.BidiGenerateContent?key=$apiKey"
    }

    override fun setOnEventListener(l: IWebSocketManager.OnEventListener?) {
        listener = l
    }

    override fun connect() {
        scope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(getUrl(ApiModelProvider.getNextKey()))
                .build()
            webSocket = client.newWebSocket(request, webSocketListener!!)
        }
    }

    override fun sendMessage(content: ClientContentMessage) {
        timeoutJob?.cancel()
        timeoutJob = scope.launch(Dispatchers.IO) {
            val timeoutMillis = 2000L // 2秒超时
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - lastOnMsg > timeoutMillis) {
                if (System.currentTimeMillis() - startTime > timeoutMillis) {
                    connect()
                    break
                }
                delay(100)
            }
        }

        scope.launch {
            webSocket?.send(content.toJson()) ?: logE(TAG, "WebSocket 未连接")
        }
    }

    override fun sendMessage(raw: String) {
        scope.launch {
            webSocket?.send(raw) ?: logE(TAG, "WebSocket 未连接")
        }
    }

    @Synchronized
    override fun close() {
        webSocket?.close(1000, "正常关闭")
        webSocket = null
        audioPlayer.release()
        messageParser.isSetupComplete = false
    }

    private fun sendSetupMessage() {
        val setupMessage = BidiGenerateContentSetup(getSetupConfig())
        sendMessage(setupMessage.toJson())
    }

    override var webSocketListener: WebSocketListener? = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            logE(TAG, "WebSocket 连接已打开")

            this@WebSocketManager.webSocket = webSocket
            sendSetupMessage()
            scope.launch { audioPlayer.initialize(24000) }

            listener?.onEvent(IWebSocketManager.Event.Opened)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            logE(TAG, "WebSocket 接收 ${text.length} 字")
            lastOnMsg = System.currentTimeMillis()

            when (val result = messageParser.parseTextMessage(text)) {
                is ParsedResult.Audio -> {
                    if (result.sampleRate != audioPlayer.sampleRate) {
                        audioPlayer.initialize(result.sampleRate)
                    }
                    audioPlayer.playAudio(result.pcmData)
                }

                ParsedResult.SetupCompleted ->
                    listener?.onEvent(IWebSocketManager.Event.SetupCompleted)

                is ParsedResult.OutputTranscription ->
                    listener?.onEvent(IWebSocketManager.Event.Message(result.text))

                ParsedResult.TurnComplete ->
                    listener?.onEvent(IWebSocketManager.Event.TurnComplete)

                ParsedResult.GoAway ->
                    close()

                null ->
                    logE(TAG, "ws 解析失败:\n$text")
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            onMessage(webSocket, bytes.utf8())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            logE(TAG, "WebSocket 连接正在关闭: $code $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            logE(TAG, "WebSocket 连接已关闭: $code $reason")

            audioPlayer.release()
            listener?.onEvent(IWebSocketManager.Event.Down(null))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.logE(TAG)

            audioPlayer.release()
            listener?.onEvent(IWebSocketManager.Event.Down(t))
        }
    }
}