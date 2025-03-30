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