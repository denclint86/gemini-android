package com.tv.app.accessibility

import android.graphics.Rect
import com.google.gson.annotations.SerializedName

data class Node(
    @SerializedName("text") val text: String,
    @SerializedName("class") val className: String?,
    val rect: NRect,
) {
    data class NRect(
        @SerializedName("l") var left: Int?,
        @SerializedName("t") var top: Int?,
        @SerializedName("r") var right: Int?,
        @SerializedName("b") var bottom: Int?,
    )
}

fun Rect.toNRect() = Node.NRect(left, top, right, bottom)
