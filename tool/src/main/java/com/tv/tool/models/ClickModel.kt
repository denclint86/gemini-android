package com.tv.tool.models

import com.google.ai.client.generativeai.type.Schema

data object ClickModel : ShellExecutorModel() {
    override val name: String = "click"
    override val description: String =
        "通过屏幕坐标模拟点击操作. 需确保目标视图在屏幕上可见, 建议结合 ${ScreenContentModel.name} 获取坐标"
    override val parameters: List<Schema<*>> = listOf(
        Schema.int("x", "点击的 X 坐标, 单位为像素"),
        Schema.int("y", "点击的 Y 坐标, 单位为像素"),
        Schema.str("type", "可选. 点击类型. (Options: 'single', 'long'). 默认 'single'")
    )
    override val requiredParameters: List<String> = listOf("x", "y")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val x = args.readAsString("x") ?: return errorFuncCallMap()
        val y = args.readAsString("y") ?: return errorFuncCallMap()

        val clickType = args["type"]?.toString() ?: "single"

        val command = when (clickType) {
            "long" -> "input tap $x $y 1500" // 模拟长按, 持续 1000ms
            else -> "input tap $x $y" // 模拟单次点击
        }

        return runShell(command)
    }
}