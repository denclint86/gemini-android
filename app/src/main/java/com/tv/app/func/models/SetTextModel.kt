package com.tv.app.func.models

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.google.ai.client.generativeai.type.Schema
import com.tv.app.accessibility.MyAccessibilityService

fun AccessibilityService.setTextToFocusedField(text: String): Boolean {
    val focusedNode = findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
    focusedNode.let { node ->
        try {
            if (!node.isEditable)
                return false

            Bundle().let {
                it.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    text
                )
                return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, it)
            }
        } catch (e: Exception) {
            return false
        } finally {
            node.recycle()
        }
    }
}
//
//fun AccessibilityService.findAndSetTextToEditableField(text: String): Boolean {
//    // 获取当前焦点节点
//    val focusedNode = findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
//        ?: findFocus(AccessibilityNodeInfo.FOCUS)
//
//    if (focusedNode == null) return false
//
//    // 如果当前节点可编辑，直接设置文本
//    if (focusedNode.isEditable) {
//        val arguments = Bundle()
//        arguments.putCharSequence(
//            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
//            text
//        )
//        val result = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
//        focusedNode.recycle()
//        return result
//    }
//
//    // 在当前窗口中查找所有可编辑节点
//    val rootNode = rootInActiveWindow ?: return false
//    val editableNodes = ArrayList<AccessibilityNodeInfo>()
//    findEditableNodes(rootNode, editableNodes)
//
//    // 如果没有找到可编辑节点，返回失败
//    if (editableNodes.isEmpty()) {
//        focusedNode.recycle()
//        rootNode.recycle()
//        return false
//    }
//
//    // 尝试找到最近的可编辑节点（简单实现：取第一个）
//    val targetNode = editableNodes[0]
//    val arguments = Bundle()
//    arguments.putCharSequence(
//        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
//        text
//    )
//    val result = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
//
//    // 回收所有节点
//    editableNodes.forEach { it.recycle() }
//    focusedNode.recycle()
//    rootNode.recycle()
//
//    return result
//}

/**
 * 辅助函数：递归查找所有可编辑节点
 */
private fun findEditableNodes(
    node: AccessibilityNodeInfo?,
    resultList: ArrayList<AccessibilityNodeInfo>
) {
    if (node == null) return

    if (node.isEditable) {
        resultList.add(node)
    }

    for (i in 0 until node.childCount) {
        val childNode = node.getChild(i) ?: continue
        findEditableNodes(childNode, resultList)
    }
}

data object SetTextModel : BaseFuncModel() {
    override val name: String = "set_text_to_focus_field"
    override val description: String =
        "Sets the specified UTF-8 text string to the currently focused input field on the user's device."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("text", "the text to set to the focused input field."),
    )
    override val requiredParameters: List<String> = listOf("text")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val service = MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()

        val text = args.readAsString("text") ?: return errorFuncCallMap()

        try {
            service.setTextToFocusedField(text)
            return okMap()
        } catch (t: Throwable) {
            return defaultMap("error", t.toSimpleLog())
        }
    }
}