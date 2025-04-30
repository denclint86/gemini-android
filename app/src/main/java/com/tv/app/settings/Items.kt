package com.tv.app.settings

import com.tv.app.ui.Setting
import kotlinx.coroutines.runBlocking

object Items {
    fun getAll(): List<Setting<*>> {
        return listOf(
            Index,
            SleepTime,
            ModelName,
            SystemPrompt,
            Temperature,
            MaxOutputTokens,
            TopP,
            TopK,
            CandidateCount,
            FrequencyPenalty
        )
    }

    object Index : Setting<Int>() {
        override val name: String
            get() = "索引"
        override val preview: Int
            get() = runBlocking { SettingsRepository.getIndex() }

        override fun onValidate(v: Any): Boolean {
            return v is Int && v >= 0
        }
    }

    object SleepTime : Setting<Long>() {
        override val name: String
            get() = "休眠时间 (毫秒)"
        override val preview: Long
            get() = runBlocking { SettingsRepository.getSleepTime() }

        override fun onValidate(v: Any): Boolean {
            return v is Long && v >= 0
        }
    }

    object ModelName : Setting<String>() {
        override val name: String
            get() = "模型名称"
        override val preview: String
            get() = runBlocking { SettingsRepository.getModelName() }

        override fun onValidate(v: Any): Boolean {
            return v is String && v.isNotEmpty()
        }
    }

    object SystemPrompt : Setting<String>() {
        override val name: String
            get() = "系统提示"
        override val preview: String
            get() = runBlocking { SettingsRepository.getSystemPrompt() }

        override fun onValidate(v: Any): Boolean {
            return v is String
        }
    }

    object Temperature : Setting<Float>() {
        override val name: String
            get() = "温度"
        override val preview: Float
            get() = runBlocking { SettingsRepository.getTemperature() }

        override fun onValidate(v: Any): Boolean {
            return v is Float && v in 0.0f..2.0f
        }
    }

    object MaxOutputTokens : Setting<Int>() {
        override val name: String
            get() = "最大输出令牌数"
        override val preview: Int
            get() = runBlocking { SettingsRepository.getMaxOutputTokens() }

        override fun onValidate(v: Any): Boolean {
            return v is Int && v in 1..4096
        }
    }

    object TopP : Setting<Float>() {
        override val name: String
            get() = "Top P"
        override val preview: Float
            get() = runBlocking { SettingsRepository.getTopP() }

        override fun onValidate(v: Any): Boolean {
            return v is Float && v in 0.0f..1.0f
        }
    }

    object TopK : Setting<Int>() {
        override val name: String
            get() = "Top K"
        override val preview: Int
            get() = runBlocking { SettingsRepository.getTopK() }

        override fun onValidate(v: Any): Boolean {
            return v is Int && v in 1..100
        }
    }

    object CandidateCount : Setting<Int>() {
        override val name: String
            get() = "候选数量"
        override val preview: Int
            get() = runBlocking { SettingsRepository.getCandidateCount() }

        override fun onValidate(v: Any): Boolean {
            return v is Int && v in 1..4
        }
    }

    object FrequencyPenalty : Setting<Float>() {
        override val name: String
            get() = "频率惩罚"
        override val preview: Float
            get() = runBlocking { SettingsRepository.getFrequencyPenalty() }

        override fun onValidate(v: Any): Boolean {
            return v is Float && v in 0.0f..2.0f
        }
    }
}