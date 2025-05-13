package com.tv.app.model


import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.tv.app.settings.intances.CandidateCount
import com.tv.app.settings.intances.FrequencyPenalty
import com.tv.app.settings.intances.MaxOutputTokens
import com.tv.app.settings.intances.ModelName
import com.tv.app.settings.intances.PresencePenalty
import com.tv.app.settings.intances.Setting
import com.tv.app.settings.intances.SystemPrompt
import com.tv.app.settings.intances.Temperature
import com.tv.app.settings.intances.Timeout
import com.tv.app.settings.intances.Tools
import com.tv.app.settings.intances.TopK
import com.tv.app.settings.intances.TopP
import com.tv.app.settings.values.Default
import com.tv.app.utils.getSealedClassObjects
import kotlin.reflect.full.createInstance

object SettingsRepository {
    private val _settingMap = mutableMapOf<String, Setting<*>>()
    val settingMap: Map<String, Setting<*>>
        get() = _settingMap

    inline fun <reified T : Setting<*>> get(): T? =
        settingMap.values.firstOrNull { it is T } as? T

    init {
        val list = getSealedClassObjects(Setting::class) { kClass ->
            if (!kClass.isAbstract && kClass.constructors.any { it.parameters.isEmpty() }) {
                kClass.createInstance()
            } else {
                null
            }
        }

        list.sortedBy { it.name }.forEach { setting ->
            _settingMap[setting.name] = setting
        }
    }

    private fun getTools(): List<Tool>? {
        return if (get<Tools>()?.isEnabled() != false)
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
            temperature = get<Temperature>()?.value()
            maxOutputTokens = get<MaxOutputTokens>()?.value()
            topP = get<TopP>()?.value()
            topK = get<TopK>()?.value()
            candidateCount = get<CandidateCount>()?.value()
            presencePenalty = get<PresencePenalty>()?.value()
            frequencyPenalty = get<FrequencyPenalty>()?.value()
        }
    }

    fun createGenerativeModel(key: String): GenerativeModel {
        return GenerativeModel(
            modelName = get<ModelName>()?.value(true) ?: Default.MODEL_NAME,
            apiKey = key,
            systemInstruction = content {
                text(get<SystemPrompt>()?.value(true) ?: Default.SYSTEM_PROMPT)
            },
            tools = getTools(),
            generationConfig = createGenerationConfig(),
            safetySettings = getSafetySettings(),
            requestOptions = RequestOptions(
                timeout = get<Timeout>()?.value(true) ?: Default.TIMEOUT_MS, // 超时
                apiVersion = "v1beta", // api 版本
            )
        )
    }
}