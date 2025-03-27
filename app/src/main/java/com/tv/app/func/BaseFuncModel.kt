package com.tv.app.func

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.defineFunction

/**
 * 函数基类
 */
abstract class BaseFuncModel<T> {
    abstract val name: String // 函数名
    abstract val description: String // 函数的功能描述
    abstract val parameters: List<Schema<*>> // 各个变量的定义
    abstract val requiredParameters: List<String> // 要求的输入参数

    abstract fun call(args: Map<String, Any?>): T

    fun getFuncDeclaration(): FunctionDeclaration = defineFunction(
        name = name,
        description = description,
        parameters = parameters,
        requiredParameters = requiredParameters
    )

    fun getFuncInstance() = ::call
}