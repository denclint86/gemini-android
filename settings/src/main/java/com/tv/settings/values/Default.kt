package com.tv.settings.values

import com.google.ai.client.generativeai.type.Tool
import com.tv.settings.beans.Language
import com.tv.settings.beans.Voice
import com.tv.tool.FuncManager

object Default {
    const val INDEX = 0
    const val SLEEP_TIME = 0L
    const val TIMEOUT_MS = 20_000L
    const val STREAM = true

    const val API_VERSION = "v1beta"
    val MODEL_NAME = Model.GEMINI_2_5_FLASH_PREVIEW_04_17.string
    val LIVE_VOICE_NAME = Voice.Leda.string
    val LIVE_LANGUAGE = Language.ENGLISH_US.string
    const val LIVE_PROMPT: String = MayaPrompt // NormalPrompt
    val SYSTEM_PROMPT: String = AutoCtrlPrompt // SimplePrompt

    const val TEMPERATURE = 0.7f
    const val TOOLS = true
    const val LIVE = false
    const val MAX_OUTPUT_TOKENS = 1024 * 8
    const val TOP_P = 1.0f
    const val TOP_K = 60
    const val CANDIDATE_COUNT = 1
    const val PRESENCE_PENALTY = 0.6f
    const val FREQUENCY_PENALTY = 0.3f

    val APP_TOOLS: List<Tool>? by lazy {
        listOf(
            Tool(functionDeclarations = FuncManager.getDeclarations())
        )
//        null
    }
}