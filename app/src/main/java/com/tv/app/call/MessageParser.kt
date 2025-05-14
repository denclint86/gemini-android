package com.tv.app.call

import android.util.Base64
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import okio.ByteString
import org.json.JSONObject

// 消息解析实现
class MessageParser : IMessageParser {
    private var isSetupComplete = false

    override fun parseTextMessage(text: String): Boolean {
        if (text.contains("setupComplete")) {
            isSetupComplete = true
            return true
        }
        return false
    }

    override fun parseBinaryMessage(bytes: ByteString): AudioData? {
        return try {
            val jsonString = bytes.utf8()
            val json = JSONObject(jsonString)
            val inlineData = json
                .getJSONObject("serverContent")
                .getJSONObject("modelTurn")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getJSONObject("inlineData")

            val mimeType = inlineData.getString("mimeType")
            val base64Data = inlineData.getString("data")

            if (!mimeType.startsWith("audio/pcm")) {
                logE(TAG, "非 PCM 音频数据: $mimeType")
                return null
            }
            val sampleRate = mimeType.split(";rate=").getOrNull(1)?.toIntOrNull() ?: 24000
            val pcmData = Base64.decode(base64Data, Base64.DEFAULT)
            AudioData(pcmData, sampleRate)
        } catch (e: Exception) {
            logE(TAG, "PCM 音频处理失败: ${e.message}")
            null
        }
    }

    override fun isSetupComplete(): Boolean = isSetupComplete
}