package com.tv.app.call

import android.util.Base64
import com.tv.app.call.beans.Data
import com.tv.app.call.beans.ParsedResult
import com.tv.app.utils.toJsonClass
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import okio.ByteString


class MessageParser : IMessageParser {
    private var isSetupComplete = false

    override fun parseTextMessage(text: String): ParsedResult? {
        try {
            logE(TAG, text)
            if (text.contains("setupComplete")) {
                isSetupComplete = true
                return ParsedResult.SetupCompleted
            } else {
                val json = text.toJsonClass<Data.Blob>()

                json?.serverContent?.modelTurn?.parts?.get(0)?.inlineData?.let { inlineData ->
                    val mimeType = inlineData.mimeType ?: return null
                    val base64Data = inlineData.data ?: return null

                    if (!mimeType.startsWith("audio/pcm")) {
                        logE(TAG, "非 PCM 音频数据: $mimeType")
                        return null
                    }
                    val sampleRate = mimeType.split(";rate=").getOrNull(1)?.toIntOrNull() ?: 24000
                    val pcmData = Base64.decode(base64Data, Base64.DEFAULT)

                    return ParsedResult.Audio(pcmData, sampleRate)
                }
            }
        } catch (e: Exception) {
            e.logE(TAG)
        }
        return null
    }

    override fun parseBinaryMessage(bytes: ByteString): ParsedResult? {
        return parseTextMessage(bytes.utf8())
    }

    override fun isSetupComplete(): Boolean = isSetupComplete
}