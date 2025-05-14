package com.tv.app.call.beans

data class WebSocketConfig(
    val apiVersion: String = "v1beta",
    val modelName: String = "gemini-2.0-flash-exp",
    val contentType: String = "AUDIO"
)