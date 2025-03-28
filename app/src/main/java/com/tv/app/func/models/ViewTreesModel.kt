package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.accessibility.AccessibilityTreeManager

data object ViewTreesModel : BaseFuncModel() {
    override val name: String = "get_device_view_trees"
    override val description: String =
        "Uses an Accessibility Service to get the current UI structure (view hierarchy) of user's Android device and returns it as a JSON string. It might be error message."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("default", "Just simply pass in 0.")
    )
    override val requiredParameters: List<String> = listOf("default")
    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val tree = AccessibilityTreeManager.nodeTree ?: return defaultMap(
            "error",
            "accessibility service is unavailable."
        )

        return defaultMap("ok", tree)
    }
}