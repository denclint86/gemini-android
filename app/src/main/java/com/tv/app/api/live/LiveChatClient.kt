package com.tv.app.api.live

import com.tv.app.api.live.beans.AudioTranscriptionConfig
import com.tv.app.api.live.beans.GenerationConfig
import com.tv.app.api.live.beans.SetupConfig
import com.tv.app.api.live.beans.SpeechConfig
import com.tv.app.api.live.beans.SystemContent
import com.tv.app.api.live.interfaces.IAudioPlayer
import com.tv.app.api.live.interfaces.IMessageParser
import com.tv.app.api.live.interfaces.IWebSocketManager
import com.tv.settings.getSetting
import com.tv.settings.intances.LiveLanguage
import com.tv.settings.intances.LivePrompt
import com.tv.settings.intances.LiveVoiceName
import kotlinx.coroutines.CoroutineScope


class LiveChatClient(scope: CoroutineScope) {
    private val messageParser: IMessageParser = MessageParser()
    private val audioPlayer: IAudioPlayer = AudioPlayer()

    private val manager: IWebSocketManager =
        object : WebSocketManager(scope, messageParser, audioPlayer) {
            override fun getSetupConfig(): SetupConfig {
                return SetupConfig(
                    model = "models/gemini-2.0-flash-exp",
                    generationConfig = GenerationConfig(
                        responseModalities = listOf("AUDIO"),
                        speechConfig = SpeechConfig(
                            getSetting<LiveLanguage>()?.value(true) ?: com.tv.settings.values.Default.LIVE_LANGUAGE,
                            getSetting<LiveVoiceName>()?.value(true) ?: com.tv.settings.values.Default.LIVE_VOICE_NAME
                        ),
                        temperature = 2.0F
                    ),
                    systemInstruction = SystemContent(
                        getSetting<LivePrompt>()?.value(true) ?: com.tv.settings.values.Default.LIVE_PROMPT
                    ),
                    inputAudioTranscription = AudioTranscriptionConfig,
                    outputAudioTranscription = AudioTranscriptionConfig
                )
            }
        }

    val isAlive: Boolean
        get() = manager.isAlive

    val isSetupComplete: Boolean
        get() = messageParser.isSetupComplete

    fun setOnEventListener(l: IWebSocketManager.OnEventListener?) =
        manager.setOnEventListener(l)

    fun connect() =
        manager.connect()

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