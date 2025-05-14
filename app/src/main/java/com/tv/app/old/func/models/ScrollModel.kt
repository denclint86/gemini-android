package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.old.accessibility.MyAccessibilityService
import com.tv.app.old.accessibility.scroll

data object ScrollModel : BaseFuncModel() {
    override val name: String
        get() = "perform_scroll"
    override val description: String
        get() = "在用户屏幕上执行滚动操作. 注意: 为提高效率, 请滚动更大的幅度"
    override val parameters = listOf(
        Schema.double("startX", "起始点 X, 单位为像素"),
        Schema.double("startY", "起始点 Y, 单位为像素"),
        Schema.double("endX", "结束点 X, 单位为像素"),
        Schema.double("endY", "结束点 Y, 单位为像素"),
        Schema.long("duration", "滚动的持续时间, 单位为毫秒")
    )
    override val requiredParameters: List<String> =
        listOf("startX", "startY", "endX", "endY", "duration")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val service = MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()

        val startX = args.readAsString("startX")?.toFloatOrNull() ?: return errorFuncCallMap()
        val startY = args.readAsString("startY")?.toFloatOrNull() ?: return errorFuncCallMap()
        val endX = args.readAsString("endX")?.toFloatOrNull() ?: return errorFuncCallMap()
        val endY = args.readAsString("endY")?.toFloatOrNull() ?: return errorFuncCallMap()
        val duration = args.readAsString("duration")?.toLongOrNull() ?: return errorFuncCallMap()

        val dispatched = service.scroll(startX, startY, endX, endY, 0, duration)
        return if (dispatched)
            successMap()
        else
            errorMap("动作未被执行")
    }
}