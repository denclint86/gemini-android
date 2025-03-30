package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.accessibility.AccessibilityListManager

data object VisibleViewsModel : BaseFuncModel() {
    override val name: String = "get_screen_content"
    override val description: String =
        "Get the content of the screen as JSON with android rect. A screenshot may be attached."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("default", "Just simply pass in 0.")
    )
    override val requiredParameters: List<String> = listOf("default")
    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val map = AccessibilityListManager.nodeMap ?: return defaultMap(
            "error",
            "accessibility service is unavailable."
        )

        return map
    }
}