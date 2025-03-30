package com.tv.app.func.models

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.defineFunction

fun defaultMap(status: String, result: Any = "") =
    mapOf<String, Any?>("status" to status, "result" to result)

fun accessibilityErrorMap() = defaultMap(
    "error",
    "accessibility service is unavailable."
)

fun okMap() = defaultMap("ok")

fun errorFuncCallMap() = defaultMap(
    "error",
    "incorrect function calling"
)

/**
 * 函数基类
 */
sealed class BaseFuncModel {
    abstract val name: String // 函数名
    abstract val description: String // 函数的功能描述
    abstract val parameters: List<Schema<*>> // 各个变量的定义
    abstract val requiredParameters: List<String> // 要求的输入参数

    /**
     * 用于调用的函数本体，为了方便转 json，直接返回 map
     */
    abstract suspend fun call(args: Map<String, Any?>): Map<String, Any?>

    fun getFuncDeclaration(): FunctionDeclaration = defineFunction(
        name = name,
        description = description,
        parameters = parameters,
        requiredParameters = requiredParameters
    )

    fun getFuncInstance() = ::call

    protected fun <T> Map<T, Any?>.readAsString(key: T): String? = (get(key) as? String)
}