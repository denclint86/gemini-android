package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema

data object OpenUriModel : ShellExecutorModel() {
    override val name: String = "open_uri"
    override val description: String =
        "通过传入 uri 在用户的设备上运行安卓隐式 intent. " +
                "可以打开如浏览器, 文件管理器, 拨号器, 地图等. " +
                "传入的 uri 可以是 https://, file://, tel:, geo://等"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("uri", "uri 地址")
    )
    override val requiredParameters: List<String> = listOf("uri")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val uri = args.readAsString("uri")
            ?: return errorFuncCallMap()

        // 使用shell命令触发隐式Intent，uri需加引号以处理特殊字符
        return runShell(
            "am start -a android.intent.action.VIEW -d \"$uri\""
        )
    }
}