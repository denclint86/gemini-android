package com.tv.app.settings

enum class Model(val value: String) {
    GEMINI_2_5_PRO_EXP("gemini-2.5-pro-exp-03-25"), // 不支持函数
    GEMINI_2_5_PRO_PREVIEW("gemini-2.5-pro-preview-03-25"),
    GEMINI_2_0_FLASH_THINKING_EXP("gemini-2.0-flash-thinking-exp-01-21"), // 不支持函数
    GEMINI_2_0_FLASH_EXP("gemini-2.0-flash-exp"),
    GEMINI_2_0_FLASH("gemini-2.0-flash"),
}