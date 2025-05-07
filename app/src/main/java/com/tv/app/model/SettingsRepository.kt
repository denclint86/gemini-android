package com.tv.app.model


import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.tv.app.settings.Setting
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

object SettingsRepository {
    val indexSetting = Index()
    val sleepTimeSetting = SleepTime()
    val streamSetting = Stream()

    val modelNameSetting = ModelName()
    val systemPromptSetting = SystemPrompt()

    val temperatureSetting = Temperature()
    val toolsSetting = Tools()
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
        Names.TOOLS to toolsSetting,
        Names.MAX_OUTPUT_TOKENS to maxOutputTokensSetting,
        Names.TOP_P to topPSetting,
        Names.TOP_K to topKSetting,
        Names.CANDIDATE_COUNT to candidateCountSetting,
        Names.PRESENCE_PENALTY to presencePenaltySetting,
        Names.FREQUENCY_PENALTY to frequencyPenaltySetting
    )

    private fun getTools(): List<Tool>? {
        return if (toolsSetting.isEnabled())
            Default.APP_TOOLS
        else
            null
    }

    private fun getSafetySettings(): List<SafetySetting>? {
//        return null
        return listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )
    }

    private fun createGenerationConfig(): GenerationConfig {
        return generationConfig {
            temperature = temperatureSetting.value()
            maxOutputTokens = maxOutputTokensSetting.value()
            topP = topPSetting.value()
            topK = topKSetting.value()
            candidateCount = candidateCountSetting.value()
            presencePenalty = presencePenaltySetting.value()
            frequencyPenalty = frequencyPenaltySetting.value()
        }
    }

    fun createGenerativeModel(key: String): GenerativeModel {
        return GenerativeModel(
            modelName = modelNameSetting.value(true) ?: Default.MODEL_NAME,
            apiKey = key,
            systemInstruction = content {
                text(systemPromptSetting.value(true) ?: Default.SYSTEM_PROMPT)
            },
            tools = getTools(),
            generationConfig = createGenerationConfig(),
            safetySettings = getSafetySettings(),
//            requestOptions = RequestOptions(),
        ).also {
            GlobalScope.launch(Dispatchers.IO) {
                logE(TAG, it.generationConfig.toPrettyJson())
                logE(TAG, streamSetting.value(true).toString())
            }
        }
    }
}