package com.tv.utils.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import com.tv.utils.accessibility.beans.Node

interface IAccessibility {
    val viewMap: Map<String, Node>?
    val currentApp: Pair<String, String>?

    suspend fun getNodeByHash(hash: String): AccessibilityNodeInfo?

    fun scrollScreen(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        startTime: Long = 0L,
        duration: Long = 500L,
        callback: AccessibilityService.GestureResultCallback? = null
    ): Boolean
}