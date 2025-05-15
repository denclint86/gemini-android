package com.tv.app.call

import com.tv.app.call.beans.AudioTranscriptionConfig
import com.tv.app.call.beans.GenerationConfig
import com.tv.app.call.beans.SetupConfig
import com.tv.app.call.beans.SpeechConfig
import com.tv.app.call.beans.SystemContent
import com.tv.app.model.getSetting
import com.tv.app.settings.intances.LiveLanguage
import com.tv.app.settings.intances.LivePrompt
import com.tv.app.settings.intances.LiveVoiceName
import com.tv.app.settings.values.Default
import kotlinx.coroutines.CoroutineScope


class LiveChatClient(scope: CoroutineScope) {
    private val messageParser: IMessageParser = MessageParser()
    private val audioPlayer: IAudioPlayer = AudioPlayer()

    private val manager: IWebSocketManager = object : WSManager(scope, messageParser, audioPlayer) {
        override fun getSetupConfig(): SetupConfig {
            return SetupConfig(
                model = "models/gemini-2.0-flash-exp",
                generationConfig = GenerationConfig(
                    responseModalities = listOf("AUDIO"),
                    speechConfig = SpeechConfig(
                        getSetting<LiveLanguage>()?.value(true) ?: Default.LIVE_LANGUAGE,
                        getSetting<LiveVoiceName>()?.value(true) ?: Default.LIVE_VOICE_NAME
                    ),
                    temperature = 2.0F
                ),
                systemInstruction = SystemContent(
                    getSetting<LivePrompt>()?.value(true) ?: Default.LIVE_PROMPT
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