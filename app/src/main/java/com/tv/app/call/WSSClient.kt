package com.tv.app.call

import kotlinx.coroutines.CoroutineScope


class WSSClient(scope: CoroutineScope) {
    private val manager: IWebSocketManager = WSSManager(
        scope,
        MessageParser(),
        AudioPlayer()
    )

    fun setOnEventListener(l: IWebSocketManager.OnEventListener?) =
        manager.setOnEventListener(l)

    fun connect(apiKey: String) =
        manager.connect(apiKey)

    fun sendClientContent(text: String) =
        manager.sendMessage(text)

    fun sendClientContent(content: WSSManager.ClientContentMessage) =
        manager.sendMessage(content)

    fun close() =
        manager.close()
}