package com.tv.app.settings.v2

import com.google.ai.client.generativeai.type.Tool
import com.tv.app.settings.Model
import com.tv.app.settings.PROMPT

object Default {
    const val INDEX = 0
    const val SLEEP_TIME = 0L
    const val STREAM = false

    val MODEL_NAME = Model.GEMINI_2_5_FLASH_PREVIEW_04_17.string
    const val SYSTEM_PROMPT: String = PROMPT

    const val TEMPERATURE = 1.2f
    const val MAX_OUTPUT_TOKENS = 2048
    const val TOP_P = 0.95f
    const val TOP_K = 40
    const val CANDIDATE_COUNT = 1
    const val PRESENCE_PENALTY = 0.6f
    const val FREQUENCY_PENALTY = 0.6f

    val APP_TOOLS: List<Tool>? by lazy {
//            listOf(
//                Tool(functionDeclarations = FuncManager.getDeclarations())
//            )
        null
    }
}