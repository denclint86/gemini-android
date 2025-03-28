package com.tv.app.chat

import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content

enum class Role(val str: String?) {
    USER("user"),
    MODEL("model"),
    SYSTEM("system"),
    FUNC("function")
}

fun isGeminiSupported(role: Role): Boolean {
    return role == Role.USER || role == Role.MODEL
}

fun userContent(init: Content.Builder.() -> Unit) = content(role = Role.USER.str, init)
fun funcContent(init: Content.Builder.() -> Unit) = content(role = Role.FUNC.str, init)
fun modelContent(init: Content.Builder.() -> Unit) = content(role = Role.MODEL.str, init)

fun createContent(role: Role = Role.USER, init: Content.Builder.() -> Unit): Content {
    if (isGeminiSupported(role))
        return content(role.str, init)
    else
        throw IllegalArgumentException("gemini 不支持这个角色 ($role)")
}