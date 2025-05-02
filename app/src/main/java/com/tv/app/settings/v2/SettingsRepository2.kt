package com.tv.app.settings.v2


import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.tv.app.settings.v2.intances.CandidateCount
import com.tv.app.settings.v2.intances.FrequencyPenalty
import com.tv.app.settings.v2.intances.Index
import com.tv.app.settings.v2.intances.MaxOutputTokens
import com.tv.app.settings.v2.intances.ModelName
import com.tv.app.settings.v2.intances.PresencePenalty
import com.tv.app.settings.v2.intances.SleepTime
import com.tv.app.settings.v2.intances.SystemPrompt
import com.tv.app.settings.v2.intances.Temperature
import com.tv.app.settings.v2.intances.TopK
import com.tv.app.settings.v2.intances.TopP
import kotlinx.coroutines.runBlocking

object SettingsRepository2 {
    val indexSetting = Index()
    val sleepTimeSetting = SleepTime()

    val modelNameSetting = ModelName()
    val systemPromptSetting = SystemPrompt()

    val temperatureSetting = Temperature()
    val maxOutputTokensSetting = MaxOutputTokens()
    val topPSetting = TopP()
    val topKSetting = TopK()
    val candidateCountSetting = CandidateCount()
    val presencePenaltySetting = PresencePenalty()
    val frequencyPenaltySetting = FrequencyPenalty()


    fun getTools(): List<Tool>? = Default.APP_TOOLS

    fun createGenerationConfig(): GenerationConfig {
        return generationConfig {
            temperature = temperatureSetting.value
            maxOutputTokens = maxOutputTokensSetting.value
            topP = topPSetting.value
            topK = topKSetting.value
            candidateCount = candidateCountSetting.value
            presencePenalty = presencePenaltySetting.value
            frequencyPenalty = frequencyPenaltySetting.value
        }
    }

    fun createGenerativeModel(key: String) {
        GenerativeModel(
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
        )
    }
}