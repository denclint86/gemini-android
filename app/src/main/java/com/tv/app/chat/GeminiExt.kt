package com.tv.app.chat

import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content

enum class Role(val str: String?) {
    USER("user"),
    MODEL("model"),
    SYSTEM(null), // 区分 api 可用项
    FUNC(null)
}

fun isGeminiSupported(role: Role): Boolean {
    return role == Role.USER || role == Role.MODEL
}

fun userContent(init: Content.Builder.() -> Unit) = content(role = Role.USER.str, init)
fun modelContent(init: Content.Builder.() -> Unit) = content(role = Role.MODEL.str, init)

fun createContent(role: Role = Role.USER, init: Content.Builder.() -> Unit): Content {
    if (isGeminiSupported(role))
        return content(role.str, init)
    else
        throw IllegalArgumentException("gemini 不支持这个角色 ($role)")
}