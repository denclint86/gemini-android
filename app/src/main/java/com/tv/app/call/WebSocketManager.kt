package com.tv.app.call

import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

// 网络配置 data class
data class WebSocketConfig(
    val baseUrl: String = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage",
    val apiVersion: String = "v1beta",
    val modelName: String = "gemini-2.0-flash-exp",
    val contentType: String = "AUDIO"
)

// 请求消息体 data class
data class SetupMessage(
    val setup: SetupConfig
) {
    data class SetupConfig(
        val model: String,
        val generation_config: GenerationConfig
    ) {
        data class GenerationConfig(
            val response_modalities: List<String>
        )
    }
}

data class ClientContentMessage(
    val client_content: ClientContent
) {
    data class ClientContent(
        val turns: List<Turn>,
        val turn_complete: Boolean
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

// WebSocket 管理实现
class WebSocketManager(
    private val scope: CoroutineScope,
    private val messageParser: IMessageParser,
    private val audioPlayer: IAudioPlayer,
    private val config: WebSocketConfig = WebSocketConfig()
) : IWebSocketManager {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private var isSetupComplete = false

    override fun connect(apiKey: String) {
        val request = Request.Builder()
            .url("${config.baseUrl}.${config.apiVersion}.GenerativeService.BidiGenerateContent?key=$apiKey")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                logE(TAG, "WebSocket 连接已打开")
                this@WebSocketManager.webSocket = webSocket
                sendSetupMessage()
                scope.launch { audioPlayer.initialize(24000) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                logE(TAG, "接收到文本消息: $text")

                messageParser.parseTextMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                logE(TAG, "接收到多媒体数据")

                val raw = bytes.utf8()
                if (raw.contains("setupComplete")) {
                    messageParser.parseTextMessage(raw)

                    // 测试可用性
//                    sendClientContent("tell me about yourself, no less then 200 words")
                } else {
                    scope.launch(Dispatchers.IO) {
                        messageParser.parseBinaryMessage(bytes)?.let { audioData ->
                            if (audioData.sampleRate != audioPlayer.sampleRate) {
                                audioPlayer.initialize(audioData.sampleRate)
                            }
                            audioPlayer.playAudio(audioData.pcmData)
                        }
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                logE(TAG, "WebSocket 连接正在关闭: $code $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                logE(TAG, "WebSocket 连接已关闭: $code $reason")
                audioPlayer.release()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logE(TAG, "WebSocket 失败: ${t.message}")
                response?.let {
                    logE(TAG, "响应: ${it.code} ${it.message}")
                    it.body?.string()?.let { body -> logE(TAG, "响应体: $body") }
                }
                audioPlayer.release()
            }
        }

        scope.launch {
            withContext(Dispatchers.IO) {
                webSocket = client.newWebSocket(request, listener)
            }
        }
    }

    override fun sendMessage(message: String) {
        scope.launch {
            webSocket?.send(message) ?: logE(TAG, "WebSocket 未连接")
        }
    }

    override fun close() {
        scope.launch {
            webSocket?.close(1000, "正常关闭")
            webSocket = null
            audioPlayer.release()
        }
    }

    // 发送用户输入内容
    fun sendClientContent(text: String) {
        if (!messageParser.isSetupComplete()) {
            logE(TAG, "等待 setup 完成")
            return
        }
        val message = ClientContentMessage(
            client_content = ClientContentMessage.ClientContent(
                turns = listOf(
                    ClientContentMessage.ClientContent.Turn(
                        role = "user",
                        parts = listOf(
                            ClientContentMessage.ClientContent.Turn.Part(text)
                        )
                    )
                ),
                turn_complete = true
            )
        )
        sendMessage(message.toJson())
    }

    private fun sendSetupMessage() {
        val setupMessage = SetupMessage(
            setup = SetupMessage.SetupConfig(
                model = "models/${config.modelName}",
                generation_config = SetupMessage.SetupConfig.GenerationConfig(
                    response_modalities = listOf(config.contentType)
                )
            )
        )
        sendMessage(setupMessage.toJson())
    }
}