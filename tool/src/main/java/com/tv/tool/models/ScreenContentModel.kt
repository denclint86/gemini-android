package com.tv.tool.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.utils.accessibility.Accessibility


data object ScreenContentModel : BaseFuncModel() {
    override val name: String = "get_screen_content"
    override val description: String =
        "获取屏幕的视图树信息, 当用户发送截图后调用, 这对分析屏幕非常有用"
    override val parameters: List<Schema<*>> = defaultParameters
    override val requiredParameters: List<String> = defaultRequiredParameters

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val iAccessibility = Accessibility.instance
            ?: return accessibilityErrorMap()
        return iAccessibility.viewMap ?: accessibilityErrorMap()
    }
}