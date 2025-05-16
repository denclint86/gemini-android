package com.tv.tool.models

import com.google.ai.client.generativeai.type.Schema

data object InputKeyEventModel : ShellExecutorModel() {
    override val name: String = "send_input_key_event"
    override val description: String =
        "通过安卓 shell 命令发送键值, 可以发送例如返回键之类的键值"
    override val parameters: List<Schema<*>> = listOf(
        Schema.int("key", "要发送的键值, 通常是数字"),
    )
    override val requiredParameters: List<String> = listOf("key")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val key = args.readAsString("key") ?: return errorFuncCallMap()

        return runShell("input keyevent $key")
    }
}