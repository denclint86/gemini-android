package com.tv.app.call.beans

import com.google.ai.client.generativeai.type.Schema
import com.google.gson.annotations.SerializedName

data class BidiGenerateContentSetup(
    val setup: SetupConfig
)

data class SetupConfig(
    @SerializedName("model") val model: String, // 模型名称，例如 "gemini-2.0-flash-live-preview-04-09"
    @SerializedName("generationConfig") val generationConfig: GenerationConfig? = null, // 生成配置
    @SerializedName("systemInstruction") val systemInstruction: String? = null, // 系统指令
//    @SerializedName("tools") val tools: List<com.google.ai.client.generativeai.common.client.Tool>? = Default.APP_TOOLS?.map {it.functionDeclarations.map { d->d.toInternal() } }, // 工具定义列表
    @SerializedName("sessionResumption") val sessionResumption: SessionResumption? = null, // 会话恢复配置
    @SerializedName("input_audio_transcription") val inputAudioTranscription: AudioTranscriptionConfig? = null,
    @SerializedName("output_audio_transcription") val outputAudioTranscription: AudioTranscriptionConfig? = null
)

//data class AudioTranscription(
//    val text: String?,
//    val finished: Boolean?
//)

data object AudioTranscriptionConfig

// 生成配置
data class GenerationConfig(
    @SerializedName("candidateCount") val candidateCount: Int? = null, // 候选答案数量
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int? = null, // 最大输出 token 数
    @SerializedName("temperature") val temperature: Float? = null, // 采样温度
    @SerializedName("topP") val topP: Float? = null, // 核采样概率
    @SerializedName("topK") val topK: Int? = null, // 核采样 k 值
    @SerializedName("presencePenalty") val presencePenalty: Float? = null, // 存在惩罚
    @SerializedName("frequencyPenalty") val frequencyPenalty: Float? = null, // 频率惩罚
    @SerializedName("responseModalities") val responseModalities: List<String>? = null, // 响应模式，例如 ["TEXT", "AUDIO"]
    @SerializedName("speechConfig") val speechConfig: SpeechConfig? = null // 语音配置
)

//// 语音配置
//data class SpeechConfig1(
//    // 根据文档，speechConfig 是一个对象，但未明确字段，留空以支持扩展
//    // 可根据实际需求添加字段
//)

// 工具定义
data class Tool1(
    @SerializedName("functionDeclarations") val functionDeclarations: FunctionDeclaration? = null // 函数声明
)

// 函数声明
data class FunctionDeclaration(
    @SerializedName("name") val name: String, // 函数名称
    @SerializedName("description") val description: String? = null, // 函数描述
    @SerializedName("parameters") val parameters: Parameters? = null // 函数参数
)

// 函数参数
data class Parameters(
    @SerializedName("type") val type: String, // 参数类型，例如 "OBJECT"
    @SerializedName("properties") val properties: Map<String, ParameterProperty>? = null // 参数属性
)

// 参数属性
data class ParameterProperty(
    @SerializedName("type") val type: String, // 参数类型，例如 "STRING"
    @SerializedName("description") val description: String? = null // 参数描述
) {
    companion object {
        fun <T> from(schema: Schema<T>): ParameterProperty {
            return ParameterProperty(schema.format ?: "", schema.description)
        }
    }
}

// 会话恢复配置
data class SessionResumption(
    @SerializedName("transparent") val transparent: Boolean // 是否启用透明模式
)