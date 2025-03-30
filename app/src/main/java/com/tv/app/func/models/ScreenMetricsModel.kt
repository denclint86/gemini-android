package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.getScreenSize
import com.zephyr.log.toLogString

class ScreenMetricsModel : BaseFuncModel() {
    override val name: String = "get_screen_metrics"
    override val description: String =
        "get the screen metrics as pixels. Returns json with height and width."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("default", "Just simply pass in 0.")
    )
    override val requiredParameters: List<String> = listOf("msg")
    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        try {
            val pair = getScreenSize()
            val w = pair.first
            val h = pair.second
            return mapOf(
                "width" to w,
                "height" to h
            )
        } catch (t: Throwable) {
            return defaultMap("error", t.toLogString())
        }
    }
}