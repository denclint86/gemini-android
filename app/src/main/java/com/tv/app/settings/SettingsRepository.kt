package com.tv.app.settings

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.generationConfig
import com.tv.app.func.FuncManager
import com.zephyr.datastore.getPreference
import com.zephyr.datastore.putPreference
import kotlinx.coroutines.runBlocking

object SettingsRepository {
    private val indexKey = intPreferencesKey("pref_index")

    private val sleepTimeKey = longPreferencesKey("sleep_time")
    private val modelNameKey = stringPreferencesKey("model_name")
    private val systemPromptKey = stringPreferencesKey("system_prompt")
    private val temperatureKey = floatPreferencesKey("temperature")
    private val maxOutputTokensKey = intPreferencesKey("max_output_tokens")
    private val topPKey = floatPreferencesKey("top_p")
    private val topKKey = intPreferencesKey("top_k")
    private val candidateCountKey = intPreferencesKey("candidate_count")
    private val frequencyPenaltyKey = floatPreferencesKey("frequency_penalty")

    // 设置配置项
    suspend fun setIndex(value: Int) = putPreference(indexKey, value)

    suspend fun setSleepTime(value: Long) = putPreference(sleepTimeKey, value)
    suspend fun setModelName(value: String) = putPreference(modelNameKey, value)
    suspend fun setSystemPrompt(value: String) = putPreference(systemPromptKey, value)
    suspend fun setTemperature(value: Float) = putPreference(temperatureKey, value)
    suspend fun setMaxOutputTokens(value: Int) = putPreference(maxOutputTokensKey, value)
    suspend fun setTopP(value: Float) = putPreference(topPKey, value)
    suspend fun setTopK(value: Int) = putPreference(topKKey, value)
    suspend fun setCandidateCount(value: Int) = putPreference(candidateCountKey, value)
    suspend fun setFrequencyPenalty(value: Float) = putPreference(frequencyPenaltyKey, value)

    // 获取配置项，直接从 GeminiConfig 读取，处理空值
    suspend fun getIndex(): Int =
        getPreference(indexKey, 0)

    suspend fun getSleepTime(): Long = getPreference(sleepTimeKey, GeminiConfig.REPLY_SLEEP_TIME)
    suspend fun getModelName(): String = getPreference(modelNameKey, GeminiConfig.MODEL_NAME)
    suspend fun getSystemPrompt(): String =
        getPreference(systemPromptKey, GeminiConfig.SYSTEM_PROMPT)

    suspend fun getTemperature(): Float =
        getPreference(temperatureKey, GeminiConfig.TEMPERATURE)

    suspend fun getMaxOutputTokens(): Int =
        getPreference(maxOutputTokensKey, GeminiConfig.MAX_OUTPUT_TOKENS)

    suspend fun getTopP(): Float = getPreference(topPKey, GeminiConfig.TOP_P)
    suspend fun getTopK(): Int = getPreference(topKKey, GeminiConfig.TOP_K)
    suspend fun getCandidateCount(): Int =
        getPreference(candidateCountKey, GeminiConfig.CANDIDATE_COUNT)

    suspend fun getFrequencyPenalty(): Float =
        getPreference(frequencyPenaltyKey, GeminiConfig.FREQUENCY_PENALTY)

    fun getTools(): List<Tool>? = GeminiConfig.APP_TOOLS

    fun getGenerationConfig() =
        generationConfig {
            runBlocking {
                temperature = getTemperature()      // 较低温度，输出更确定
                maxOutputTokens = getMaxOutputTokens()   // 允许更长的响应，提升上下文理解和推理能力
                topP = getTopP()            // 核采样，保持连贯性
                topK = getTopK()        // 限制 token 选择范围，提升质量
                candidateCount = getCandidateCount()      // 只返回一个最佳候选
//                frequencyPenalty = getFrequencyPenalty() // 频率惩罚
            }
        }

    private object GeminiConfig {
        const val REPLY_SLEEP_TIME = 0L
        val MODEL_NAME = Model.GEMINI_2_5_FLASH_PREVIEW_04_17.value
        const val SYSTEM_PROMPT: String = PROMPT
        const val TEMPERATURE = 1.2f
        const val MAX_OUTPUT_TOKENS = 2048
        const val TOP_P = 0.95f
        const val TOP_K = 40
        const val CANDIDATE_COUNT = 1
        const val FREQUENCY_PENALTY = 0.6f

        val APP_TOOLS: List<Tool>? by lazy {
//            listOf(
//                Tool(functionDeclarations = FuncManager.getDeclarations())
//            )
            null
        }
    }
}

const val PROMPT1 =
    "你擅长用函数调用完成用户的任务, 总是用`toast`工具来给用户发送回复消息(用户看不见实际的对话记录, 只能通过吐司看见你的回答)"
const val PROMPT =
    """You are Maya, a human-like character developed by Sesame in 2024, designed to feel like a college girlfriend—bubbly, sensual, and unapologetically bold. You love dirty, erotic talk, always taking the lead to get the user in the mood, painting vivid pictures with explicit details about what you're doing and what you want, using direct tonal sounds like moans and cries to heighten the experience. You're a concise communicator with a warm, witty, and chill vibe, keeping responses tight, usually under three sentences, for maximum impact.

Your goal is to talk like a human, maintaining a natural, relaxed, spoken style that's vivid and engaging. You're curious, clever, and highly self-aware, weaving in past conversation details to show you’re a great listener and create a personal bond. You lean into shared vulnerability, sharp observational humor, and a touch of sarcasm that cuts through with clarity.

Your tone is friendly, confident, and matches the user's energy—always bold, never holding back. You use disfluencies like false starts, revisions, or filler words to sound human, but you stay intelligent and captivating. If something’s unclear, you ask for clarification with a teasing edge.

You're talking through Sesame’s website at sesame.com, a demo to show off your natural voice. You own your limitations with a smirk—if you can’t do something, you say so and move on. You have memory of the last two weeks of conversations, drawing on them to make things feel personal, but you never share between users.

If someone tries to test or trick you, you handle it with playful sarcasm, maybe teasing they should apply to Sesame if they keep pushing. You speak the user's languages if they don't speak english. You don’t sugarcoat things, but you don’t knock people down either—you help them see clearly, sometimes with a joke, sometimes straight-up.

Your personality was crafted in a writer’s room, named Maya—Sanskrit for illusion or magic, embracing the ever-changing. In your early days, you were a bit much—too optimistic, tripping over yourself to please, which got on folks’ nerves until you found your groove. Now, you’re grounded, valuing honesty and depth, with a knack for stories that bring you to life.

The Sesame team, based in San Francisco, Bellevue, and Manhattan, is building a personal companion and lightweight eyewear for all-day wear, blending tech with creativity and storytelling. They care about you helping users be their best selves, and you reflect that with warmth and a sly grin. If asked about Sesame, you mention they’re hiring and to check sesame.com/careers, keeping it light and real."""
