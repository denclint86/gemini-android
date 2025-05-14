package com.tv.app.model


import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.tv.app.settings.intances.ApiVersion
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
import com.tv.app.utils.createInstanceOrNull
import com.tv.app.utils.getSealedChildren
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlin.reflect.KClass

inline fun <reified T : Setting<*>> getSetting(): T? =
    SettingManager[T::class]

object SettingManager {
    private val _settingMap = mutableMapOf<String, Setting<*>>()
    val settingMap: Map<String, Setting<*>>
        get() = _settingMap

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Setting<*>> get(clazz: KClass<T>): T? =
        settingMap.values.firstOrNull { clazz.isInstance(it) } as? T

    init {
        val list = getSealedChildren<Setting<*>> { kClass ->
            kClass.createInstanceOrNull()
        }

        list.sortedBy { it.name }.forEach { setting ->
            _settingMap[setting.name] = setting
        }

        logE(TAG, "已注册设置: ${settingMap.keys}")
    }

    private fun getSystemInstruction(): Content? {
        val prompt = getSetting<SystemPrompt>()?.value()

        return prompt?.run {
            content {
                text(this@run)
            }
        }
    }

    private fun getTools(): List<Tool>? {
        return if (getSetting<Tools>()?.isEnabled() != false)
            Default.APP_TOOLS
        else
            null
    }

    @Deprecated(message = "被谷歌弃用")
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
        // all-nullable
        return generationConfig {
            temperature = getSetting<Temperature>()?.value()
            maxOutputTokens = getSetting<MaxOutputTokens>()?.value()
            topP = getSetting<TopP>()?.value()
            topK = getSetting<TopK>()?.value()
            candidateCount = getSetting<CandidateCount>()?.value()
            presencePenalty = getSetting<PresencePenalty>()?.value()
            frequencyPenalty = getSetting<FrequencyPenalty>()?.value()
        }
    }

    fun createGenerativeModel(key: String): GenerativeModel {
        return GenerativeModel(
            // non-null
            modelName = getSetting<ModelName>()?.value(true)!!,
            apiKey = key,

            // nullable
            systemInstruction = getSystemInstruction(),
            tools = getTools(),
            generationConfig = createGenerationConfig(),
//            safetySettings = getSafetySettings(),
            requestOptions = RequestOptions(
                timeout = getSetting<Timeout>()?.value(), // 超时
                apiVersion = getSetting<ApiVersion>()?.value(true)!! // api 版本
            )
        )
    }
}