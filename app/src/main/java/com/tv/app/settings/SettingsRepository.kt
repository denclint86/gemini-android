package com.tv.app.settings


import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.tv.app.settings.intances.CandidateCount
import com.tv.app.settings.intances.FrequencyPenalty
import com.tv.app.settings.intances.Index
import com.tv.app.settings.intances.MaxOutputTokens
import com.tv.app.settings.intances.ModelName
import com.tv.app.settings.intances.PresencePenalty
import com.tv.app.settings.intances.SleepTime
import com.tv.app.settings.intances.Stream
import com.tv.app.settings.intances.SystemPrompt
import com.tv.app.settings.intances.Temperature
import com.tv.app.settings.intances.Tools
import com.tv.app.settings.intances.TopK
import com.tv.app.settings.intances.TopP
import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toPrettyJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object SettingsRepository {
    val indexSetting = Index()
    val sleepTimeSetting = SleepTime()
    val streamSetting = Stream()

    val modelNameSetting = ModelName()
    val systemPromptSetting = SystemPrompt()

    val temperatureSetting = Temperature()
    val tools = Tools()
    val maxOutputTokensSetting = MaxOutputTokens()
    val topPSetting = TopP()
    val topKSetting = TopK()
    val candidateCountSetting = CandidateCount()
    val presencePenaltySetting = PresencePenalty()
    val frequencyPenaltySetting = FrequencyPenalty()

    fun getSettings(): Map<String, Setting<*>> = mapOf(
        Names.INDEX to indexSetting,
        Names.SLEEP_TIME to sleepTimeSetting,
        Names.STREAM to streamSetting,

        Names.MODEL_NAME to modelNameSetting,
        Names.SYSTEM_PROMPT to systemPromptSetting,

        Names.TEMPERATURE to temperatureSetting,
        Names.TOOLS to tools,
        Names.MAX_OUTPUT_TOKENS to maxOutputTokensSetting,
        Names.TOP_P to topPSetting,
        Names.TOP_K to topKSetting,
        Names.CANDIDATE_COUNT to candidateCountSetting,
        Names.PRESENCE_PENALTY to presencePenaltySetting,
        Names.FREQUENCY_PENALTY to frequencyPenaltySetting
    )

    private fun getTools(): List<Tool>? {
        return if (tools.isEnabled)
            Default.APP_TOOLS
        else
            null
    }

    private fun createGenerationConfig(): GenerationConfig {
        return generationConfig {
            temperature = temperatureSetting.value
            maxOutputTokens = maxOutputTokensSetting.value
            topP = topPSetting.value
            topK = topKSetting.value?.toInt()
            candidateCount = candidateCountSetting.value?.toInt()
            presencePenalty = presencePenaltySetting.value
            frequencyPenalty = frequencyPenaltySetting.value
        }
    }

    fun createGenerativeModel(key: String): GenerativeModel {
        return GenerativeModel(
            modelName = modelNameSetting.value ?: Default.MODEL_NAME,
            apiKey = key,
            systemInstruction = content {
                runBlocking {
                    text(systemPromptSetting.value ?: Default.SYSTEM_PROMPT)
                }
            },
            tools = getTools(),
            generationConfig = createGenerationConfig(),
//            safetySettings = null,
//            requestOptions = RequestOptions(),
        ).also {
            GlobalScope.launch(Dispatchers.IO) {
                logE(TAG, it.toPrettyJson())
                logE(TAG, streamSetting.value.toString())
            }
        }
    }
}