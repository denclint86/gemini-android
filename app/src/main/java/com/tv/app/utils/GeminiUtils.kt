package com.tv.app.utils

import com.google.ai.client.generativeai.type.BlobPart
import com.google.ai.client.generativeai.type.CodeExecutionResultPart
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.ExecutableCodePart
import com.google.ai.client.generativeai.type.FileDataPart
import com.google.ai.client.generativeai.type.FunctionCallPart
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.Part
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.content

enum class Role(val str: String?) {
    USER("user"),
    MODEL("model"),
    SYSTEM("system"),
    FUNC("function")
}

val Content.ROLE: Role
    get() = Role.entries.firstOrNull { it.str == role } ?: Role.USER

fun isGeminiSupported(role: Role): Boolean {
    return role == Role.USER || role == Role.MODEL
}

fun Content.toUIString(): String {
    return parts.toUIString()
}

fun GenerateContentResponse.toUIString(): String {
    return toUIString(text, functionCalls)
}

fun toUIString(text: String?, parts: List<Part>): String {
    return StringBuilder().run {
        append(text)
        if (true == text?.isNotBlank())
            append("\n")
        append(parts.toUIString())
        toString()
    }
}

fun List<Part>.toUIString(): String {
    return StringBuilder().run {
        this@toUIString.forEach { part ->
            append(part.toUIString())
            if (this@toUIString.last() != part)
                append("\n")
        }
        toString()
    }
}

fun Part.toUIString(): String {
    return when (this) {
        is TextPart -> text
        is ImagePart -> "[img]"
        is BlobPart -> "[blob]"
        is FileDataPart -> "[file]"
        is FunctionCallPart -> "[func -> $name]"
        is FunctionResponsePart -> "[func_response -> $name]"
        is ExecutableCodePart -> "[code]"
        is CodeExecutionResultPart -> "[code_result]"
        else -> "[known]"
    }
}

fun Content.toLogString(): String {
    return when (ROLE) {
        Role.USER -> "user: ${parts[0]}"
        Role.MODEL -> ""
        Role.FUNC -> ""
        Role.SYSTEM -> ""
    }
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