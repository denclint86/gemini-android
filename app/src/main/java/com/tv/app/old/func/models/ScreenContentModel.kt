package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.old.accessibility.MyAccessibilityService


data object ScreenContentModel : BaseFuncModel() {
    override val name: String = "get_screen_content"
    override val description: String =
        "获取屏幕的视图树信息以及屏幕截图"
    override val parameters: List<Schema<*>> = defaultParameters
    override val requiredParameters: List<String> = defaultRequiredParameters

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val service = MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()
        return service.getViewMap() ?: accessibilityErrorMap()
    }
}