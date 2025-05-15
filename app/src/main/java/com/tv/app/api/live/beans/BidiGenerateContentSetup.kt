package com.tv.app.api.live.beans

import com.google.gson.annotations.SerializedName
import com.tv.app.api.live.ClientContentMessage

data class BidiGenerateContentSetup(
    @SerializedName("setup") val setup: SetupConfig
)

data class SetupConfig(
    @SerializedName("model") val model: String,
    @SerializedName("generationConfig") val generationConfig: GenerationConfig? = null, // 生成配置
    @SerializedName("systemInstruction") val systemInstruction: SystemContent? = null, // 系统指令
//    @SerializedName("tools") val tools: List<com.google.ai.client.generativeai.common.client.Tool>? = Default.APP_TOOLS?.map {it.functionDeclarations.map { d->d.toInternal() } }, // 工具定义列表
    @SerializedName("sessionResumption") val sessionResumption: SessionResumption? = null, // 会话恢复配置
    @SerializedName("input_audio_transcription") val inputAudioTranscription: AudioTranscriptionConfig? = null,
    @SerializedName("output_audio_transcription") val outputAudioTranscription: AudioTranscriptionConfig? = null
)

data class SystemContent(
    @SerializedName("role") val role: String,
    @SerializedName("parts") val parts: List<ClientContentMessage.ClientContent.Turn.Part>
) {
    constructor(prompt: String) : this(
        "system",
        listOf(ClientContentMessage.ClientContent.Turn.Part(prompt))
    )
}

data object AudioTranscriptionConfig

// 生成配置
data class GenerationConfig(
//    @SerializedName("candidateCount") val candidateCount: Int? = null, // 候选答案数量
//    @SerializedName("maxOutputTokens") val maxOutputTokens: Int? = null, // 最大输出 token 数
    @SerializedName("temperature") val temperature: Float? = null, // 采样温度
//    @SerializedName("topP") val topP: Float? = null, // 核采样概率
//    @SerializedName("topK") val topK: Int? = null, // 核采样 k 值
//    @SerializedName("presencePenalty") val presencePenalty: Float? = null, // 存在惩罚
//    @SerializedName("frequencyPenalty") val frequencyPenalty: Float? = null, // 频率惩罚
    @SerializedName("responseModalities") val responseModalities: List<String>? = null, // 响应模式, 例如 ["TEXT", "AUDIO"]
    @SerializedName("speechConfig") val speechConfig: SpeechConfig? = null // 语音配置
)

data class SessionResumption(
    @SerializedName("transparent") val transparent: Boolean // 是否启用透明模式
)

data class RealtimeInputConfig(
    @SerializedName("automatic_activity_detection") val automaticActivityDetection: AutomaticActivityDetection
)

data class AutomaticActivityDetection(
    val disabled: Boolean
)

data class SpeechConfig(
    @SerializedName("language_code") val languageCode: String,
    @SerializedName("voice_config") val voiceConfig: VoiceConfig? = null
) {
    /**
     * voice: Aoede, Leda, Zephyr
     *
     * language:
     * 英语/英国: en-GB
     * 英语/美国: en-US
     * 西班牙语/美国: es-US
     * 法语/法国: fr-FR
     * 西班牙语/西班牙: es-ES
     * 印度尼西亚语/印度尼西亚: id-ID
     * 意大利语/意大利: it-IT
     * 中文普通话: 中国: cmn-CN
     */
    constructor(languageCode: String, modelName: String) : this(
        languageCode,
        VoiceConfig(modelName)
    )
}

data class VoiceConfig(
    @SerializedName("prebuilt_voice_config") val prebuiltVoiceConfig: PrebuiltVoiceConfig
) {
    /**
     * Aoede, Leda, Zephyr
     */
    constructor(modelName: String) : this(PrebuiltVoiceConfig(modelName))

    data class PrebuiltVoiceConfig(
        @SerializedName("voice_name") val voiceName: String
    )
}