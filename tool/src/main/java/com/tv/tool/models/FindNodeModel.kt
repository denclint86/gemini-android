package com.tv.tool.models


import android.view.accessibility.AccessibilityNodeInfo
import com.google.ai.client.generativeai.type.Schema
import com.tv.utils.accessibility.Accessibility
import com.tv.utils.click
import com.tv.utils.longClick
import com.tv.utils.setTextTo

data object FindNodeAndPerformModel : BaseFuncModel() {
    override val name: String = "find_node_and_perform"
    override val description: String =
        "根据哈希值对相应的视图节点进行特定的操作. " +
                "视图必须在屏幕上可见, 因此需在确保屏幕内容更新后再调用此功能. " +
                "始终使用 ${ScreenContentModel.name} 的最新结果. " +
                "注意: '节点'指的是安卓的 AccessibilityNodeInfo 类"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str(
            "hash", "调用 ${ScreenContentModel.name} 后得到的结果中, 节点的哈希值"
        ),
        Schema.str(
            "action",
            "要对节点进行的操作. (Options: " +
                    "'${Action.CLICK.v}', '${Action.LONG_CLICK.v}', " +
                    "'${Action.SET_TEXT.v}', '${Action.FOCUS.v}', " +
                    "'${Action.CLEAR_FOCUS.v}'). 注意: 此工具所指的'焦点'是安卓无障碍服务的焦点"
        ),
        Schema.str(
            "text",
            "可选. 要对节点设置的文本(action 为 '${Action.SET_TEXT}' 时有效), 只有在节点的 'isEditable' 属性为真时可用"
        ),
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
        val iAccessibility = Accessibility.instance ?: return accessibilityErrorMap()

        val hash = args.readAsString("hash") ?: return errorFuncCallMap()

        iAccessibility.getNodeByHash(hash)?.let { node ->
            node.refresh()

            val actionStr = args.readAsString("action") ?: return errorFuncCallMap()

            val action = Action.entries.find { it.v == actionStr }
                ?: return errorFuncCallMap()

            val result = when (action) {
                Action.CLICK -> node.click()

                Action.LONG_CLICK -> node.longClick()

                Action.SET_TEXT -> {
                    val text = args.readAsString("text") ?: return errorFuncCallMap()
                    node.setTextTo(text)
                }

                Action.FOCUS -> {
                    node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
                }

                Action.CLEAR_FOCUS -> {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS)
                }
            }

            node.recycle()

            return if (result)
                successMap()
            else
                errorMap("动作未被执行")
        }

        return errorMap("未找到节点")
    }
}