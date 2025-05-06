package com.tv.app.old.accessibility.node

import android.view.accessibility.AccessibilityNodeInfo
import com.tv.app.old.accessibility.MyAccessibilityService
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.zip.CRC32
import kotlin.coroutines.resume

class NodeTracker(var service: MyAccessibilityService) {

    suspend fun findNodeByHash(hash: String): AccessibilityNodeInfo? {
        val root = service.rootInActiveWindow ?: return null
        try {
            return suspendCancellableCoroutine { continuation ->
                service.traverseNode(root) { node ->
                    val currentHash = generateNodeHash(node)
                    if (currentHash == hash) {
                        if (!continuation.isCompleted)
                            continuation.resume(AccessibilityNodeInfo.obtain(node))
                        logE(TAG, "node 找到并返回, text: ${node.text.take(20)}")
                    }
                }

                if (!continuation.isCompleted) // 上面的递归时同步的，所以可以
                    continuation.resume(null)
            }
        } catch (t: Throwable) {
            return null
        } finally {
            root.recycle()
        }
    }

    fun generateNodeHash(node: AccessibilityNodeInfo): String {
        val parent = node.getParent()
        val parentHash = parent?.let { generateNodeHash(it) } ?: ""
        val properties = generateProperties(node, parentHash)
        return computeHash(properties)
    }

    private fun generateProperties(node: AccessibilityNodeInfo, parentHash: String?): String {
        val windowId = node.windowId.toString()
        val className = node.className.toString()
        val text = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        val parent = parentHash ?: ""
        return "$windowId|$className|$text|$contentDesc|$parent"
    }

    private fun computeHash(properties: String): String {
        val crc32 = CRC32()
        crc32.update(properties.toByteArray())
        return crc32.value.toString(16).padStart(8, '0') // 8-digit hex
    }

    private fun traverseAndFindNode(
        node: AccessibilityNodeInfo,
        targetHash: String
    ): AccessibilityNodeInfo? {
        val currentHash = generateNodeHash(node)
        if (currentHash == targetHash) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = traverseAndFindNode(child, targetHash)
            if (result != null) {
                return result
            }
        }
        return null
    }
}