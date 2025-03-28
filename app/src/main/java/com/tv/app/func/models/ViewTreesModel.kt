package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.accessibility.AccessibilityListManager

data object ViewTreesModel : BaseFuncModel() {
    override val name: String = "get_screen_views"
    override val description: String =
        "Uses an Accessibility Service to get the current visible views info of user's Android device and returns it as a JSON string."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("default", "Just simply pass in 0.")
    )
    override val requiredParameters: List<String> = listOf("default")
    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val list = AccessibilityListManager.nodeList ?: return defaultMap(
            "error",
            "accessibility service is unavailable."
        )

        return defaultMap("ok", list)
    }
}