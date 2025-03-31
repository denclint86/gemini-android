package com.tv.app.func.models


import android.view.accessibility.AccessibilityNodeInfo
import com.google.ai.client.generativeai.type.Schema
import com.tv.app.accessibility.MyAccessibilityService
import com.tv.app.accessibility.click
import com.tv.app.accessibility.longClick
import com.tv.app.accessibility.text

data object FindNodeModel : BaseFuncModel() {
    override val name: String = "find_node_and_do"
    override val description: String =
        "Find the view node by hash key and perform the specified action. " +
                "The view must be visible on the screen, so call this function " +
                "after ensuring screen content is updated. Always use the latest " +
                "result from ${VisibleViewsModel.name}. Note: 'node' refers " +
                "to android's AccessibilityNodeInfo class."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str(
            "hash", "The hash key from ${VisibleViewsModel.name}" +
                    "'s result. Only use the key that is sure"
        ),
        Schema.str(
            "action",
            "The action to perform on the view node. Options: " +
                    "'${Action.CLICK.v}', " +
                    "'${Action.LONG_CLICK.v}', " +
                    "'${Action.SET_TEXT.v}', '${Action.FOCUS.v}', " +
                    "'${Action.CLEAR_FOCUS.v}'. Note: focus means the focus of android accessibility service."
        ),
        Schema.str("text", "Optional. Text to set, only available for 'set text' action"),
    )
    override val requiredParameters: List<String> = listOf("hash", "action")

    private enum class Action(val v: String) {
        CLICK("click"),
        LONG_CLICK("long_click"),
        SET_TEXT("set_text"),
        FOCUS("focus"),
        CLEAR_FOCUS("clear_focus")
    }

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val service = MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()

        val hash = args.readAsString("hash") ?: return errorFuncCallMap()

        service.getNodeByHash(hash)?.let { node ->
            node.refresh()

            val actionStr = args.readAsString("action") ?: return errorFuncCallMap()

            val action = Action.entries.find { it.v == actionStr }
                ?: return errorFuncCallMap()

            val result = when (action) {
                Action.CLICK -> node.click()

                Action.LONG_CLICK -> node.longClick()

                Action.SET_TEXT -> {
                    val text = args.readAsString("text") ?: return errorFuncCallMap()
                    node.text(text)
                }

                Action.FOCUS -> {
                    node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
                }

                Action.CLEAR_FOCUS -> {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS)
                }
            }

            node.recycle()

            val status = if (result) "ok" else "failed"
            val msg = if (result) "" else "action perform failed"
            return defaultMap(status, msg)
        }

        return defaultMap("error", "node not found")
    }
}