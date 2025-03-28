package com.tv.app.accessibility

import android.graphics.Rect

data class Node(
    val text: String?,
    val className: String?,
    val rect: NRect,
    val childNode: List<Node>?
) {
    data class NRect(
        var left: Int?,
        var top: Int?,
        var right: Int?,
        var bottom: Int?,
    )
}

fun Rect.toNRect() = Node.NRect(left, top, right, bottom)
