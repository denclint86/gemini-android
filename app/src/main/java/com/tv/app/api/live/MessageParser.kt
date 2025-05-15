package com.tv.app.api.live

import android.util.Base64
import com.tv.app.api.live.beans.Data
import com.tv.app.api.live.beans.ParsedResult
import com.tv.app.api.live.interfaces.IMessageParser
import com.tv.utils.toJsonClass
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import okio.ByteString


class MessageParser : IMessageParser {
    override var isSetupComplete = false

    override fun parseTextMessage(text: String): ParsedResult? {
        try {
            when {
                text.contains("setupComplete") -> {
                    isSetupComplete = true
                    return ParsedResult.SetupCompleted
                }

                text.contains("turnComplete") -> {
                    val json = text.toJsonClass<Data.TurnComplete>()
                    json?.serverContent?.turnComplete?.let {
                        return ParsedResult.TurnComplete
                    }
                }

                text.contains("outputTranscription") -> {
                    val json = text.toJsonClass<Data.Transcription>()
                    json?.serverContent?.outputTranscription?.text?.let {
                        return ParsedResult.OutputTranscription(it)
                    }
                }

                text.contains("modelTurn") -> {
                    val json = text.toJsonClass<Data.Blob>()
                    json?.serverContent?.modelTurn?.parts?.get(0)?.inlineData?.let { inlineData ->
                        val mimeType = inlineData.mimeType ?: return null
                        val base64Data = inlineData.data ?: return null

                        if (!mimeType.startsWith("audio/pcm")) {
                            return null
                        }
                        val sampleRate =
                            mimeType.split(";rate=").getOrNull(1)?.toIntOrNull() ?: 24000
                        val pcmData = Base64.decode(base64Data, Base64.DEFAULT)

                        return ParsedResult.Audio(pcmData, sampleRate)
                    }
                }

                text.contains("goAway") ->
                    return ParsedResult.GoAway
            }
        } catch (e: Exception) {
            e.logE(TAG)
        }
        return null
    }

    override fun parseBinaryMessage(bytes: ByteString): ParsedResult? {
        return parseTextMessage(bytes.utf8())
    }
}