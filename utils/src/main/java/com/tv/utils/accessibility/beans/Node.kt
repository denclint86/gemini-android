package com.tv.utils.accessibility.beans

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.google.gson.annotations.SerializedName

data class Node(
    @SerializedName("text") val text: String?,
    @SerializedName("class") val className: String?,
    @SerializedName("is_editable") val isEditable: Boolean?,
    @SerializedName("is_accessibility_focused") val isAccessibilityFocused: Boolean?,
    val rect: NRect,
) {
    constructor(text: String?, node: AccessibilityNodeInfo, rect: Rect) : this(
        text,
        node.className?.toString(),
        if (node.isEditable) true else null,
        if (node.isAccessibilityFocused) true else null,
        rect.toNRect()
    )

    data class NRect(
        @SerializedName("l") var left: Int?,
        @SerializedName("t") var top: Int?,
        @SerializedName("r") var right: Int?,
        @SerializedName("b") var bottom: Int?,
    )
}

fun Rect.toNRect() = Node.NRect(left, top, right, bottom)
