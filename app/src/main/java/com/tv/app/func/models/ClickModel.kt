package com.tv.app.func.models

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.google.ai.client.generativeai.type.Schema
import com.tv.app.accessibility.MyAccessibilityService
//
//data object ClickModel : BaseFuncModel() {
//    override val name: String
//        get() = "perform_click"
//    override val description: String
//        get() = "Perform a click action at specific coordinates on user's screen."
//    override val parameters: List<Schema<*>> = listOf(
//        Schema.double("x", "the x coordinate to click, in pixels."),
//        Schema.double("y", "the y coordinate to click, in pixels."),
//        Schema.long("duration", "Duration of the click gesture in milliseconds, default is 100ms.")
//    )
//    override val requiredParameters: List<String> =
//        listOf("x", "y")
//
//    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
//        val service = MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()
//
//        val x = args.readAsString("x")?.toFloatOrNull() ?: return errorFuncCallMap()
//        val y = args.readAsString("y")?.toFloatOrNull() ?: return errorFuncCallMap()
//        val duration = args.readAsString("duration")?.toLongOrNull() ?: 100L
//
//        // 创建点击手势
//        val path = Path()
//        path.moveTo(x, y)
//        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
//        val gesture = GestureDescription.Builder()
//            .addStroke(stroke)
//            .build()
//
//        val dispatched = service.dispatchGesture(gesture, null, null)
//        return if (dispatched)
//            okMap()
//        else
//            defaultMap("error", "click gesture was not dispatched")
//    }
//}