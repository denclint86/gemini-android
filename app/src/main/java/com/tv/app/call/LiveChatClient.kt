package com.tv.app.call

import com.tv.app.call.WSManager.ClientContentMessage
import kotlinx.coroutines.CoroutineScope


class LiveChatClient(scope: CoroutineScope) {
    private val messageParser: IMessageParser = MessageParser()

    private val manager: IWebSocketManager = WSManager(
        scope,
        messageParser,
        AudioPlayer()
    )

    val isAlive: Boolean
        get() = manager.isAlive

    val isSetupComplete: Boolean
        get() = messageParser.isSetupComplete

    fun setOnEventListener(l: IWebSocketManager.OnEventListener?) =
        manager.setOnEventListener(l)

    fun connect(apiKey: String) =
        manager.connect(apiKey)

    fun sendClientContent(history: List<ClientContentMessage.ClientContent.Turn>) {
        val m = ClientContentMessage(
            clientContent = ClientContentMessage.ClientContent(
                turns = history,
                turnComplete = true
            )
        )
        sendClientContent(m)
    }

    fun sendClientContent(content: ClientContentMessage) =
        manager.sendMessage(content)

    fun close() =
        manager.close()
}