package com.tv.app.call

import kotlinx.coroutines.CoroutineScope


data class AudioData(val pcmData: ByteArray, val sampleRate: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioData

        if (!pcmData.contentEquals(other.pcmData)) return false
        if (sampleRate != other.sampleRate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pcmData.contentHashCode()
        result = 31 * result + sampleRate
        return result
    }
}

// 重构后的 WebSocketClient
class WebSocketClient(
    private val scope: CoroutineScope,
    private val webSocketManager: IWebSocketManager = WebSocketManager(
        scope,
        MessageParser(),
        AudioPlayer()
    )
) {
    fun connect(apiKey: String) {
        webSocketManager.connect(apiKey)
    }

    fun sendClientContent(text: String) {
        webSocketManager.sendMessage(
            """
            {
              "client_content": {
                "turns": [
                  {
                    "role": "user",
                    "parts": [
                      {
                        "text": "$text"
                      }
                    ]
                  }
                ],
                "turn_complete": true
              }
            }
        """.trimIndent()
        )
    }

    fun close() {
        webSocketManager.close()
    }
}