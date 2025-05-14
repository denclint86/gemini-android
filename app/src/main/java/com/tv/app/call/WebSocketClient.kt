package com.tv.app.call

import kotlinx.coroutines.CoroutineScope


class WebSocketClient(scope: CoroutineScope) {
    private val webSocketManager: IWebSocketManager = WebSocketManager(
        scope,
        MessageParser(),
        AudioPlayer()
    )

    fun connect(apiKey: String) =
        webSocketManager.connect(apiKey)

    fun sendClientContent(text: String) =
        webSocketManager.sendMessage(text)

    fun sendClientContent(content: WebSocketManager.ClientContentMessage) =
        webSocketManager.sendMessage(content)

    fun close() =
        webSocketManager.close()
}