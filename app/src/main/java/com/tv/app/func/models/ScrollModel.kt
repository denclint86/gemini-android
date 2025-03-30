package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.accessibility.MyAccessibilityService
import com.tv.app.accessibility.scroll

data object ScrollModel : BaseFuncModel() {
    override val name: String
        get() = "perform_scroll"
    override val description: String
        get() = "Perform a scroll action on user's screen. Note: For efficiency, please scroll as much as possible in one go."
    override val parameters: List<Schema<*>> = listOf(
        Schema.double("startX", "the gesture's x starting pos, in pixels."),
        Schema.double("startY", "the gesture's y starting pos, in pixels."),
        Schema.double("endX", "the gesture's x ending pos, in pixels."),
        Schema.double("endY", "the gesture's y ending pos, in pixels."),
        Schema.long("duration", "Duration of the scroll gesture in milliseconds.")
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
            okMap()
        else
            defaultMap("error", "gesture was not dispatched")
    }
}
//
//// 可能的 ClickViewModel 实现框架
//data object ClickViewModel : BaseFuncModel() {
//    override val name: String
//        get() = "click_view"
//    override val description: String
//        get() = "Click a specific view based on its properties."
//    override val parameters: List<Schema<*>> = listOf(
//        Schema.string("text", "Text content of the view to click (optional)"),
//        Schema.string("id", "Resource ID of the view to click (optional)"),
//        Schema.string("description", "Content description of the view to click (optional)")
//    )
//    override val requiredParameters: List<String> = emptyList() // 至少需要一个标识方式
//
//    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
//        val service = MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()
//
//        // 获取根节点
//        val rootNode = service.rootInActiveWindow ?: return defaultMap("error", "no active window")
//
//        // 查找目标节点
//        val targetNode = rootNode.findNodeOrNull {
//            val textMatch = args["text"]?.let { text?.contains(it.toString()) == true } ?: true
//            val idMatch = args["id"]?.let { viewId?.contains(it.toString()) == true } ?: true
//            val descMatch = args["description"]?.let { contentDescription?.contains(it.toString()) == true } ?: true
//            textMatch && idMatch && descMatch && isClickable
//        } ?: return defaultMap("error", "no matching clickable view found")
//
//        // 执行点击
//        val clicked = targetNode.click()
//        targetNode.recycle() // 释放节点资源
//
//        return if (clicked)
//            okMap()
//        else
//            defaultMap("error", "view click failed")
//    }
//}