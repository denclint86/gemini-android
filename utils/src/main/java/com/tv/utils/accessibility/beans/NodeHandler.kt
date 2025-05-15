package com.tv.utils.accessibility.beans

import android.view.accessibility.AccessibilityNodeInfo
import com.tv.utils.accessibility.Accessibility
import com.tv.utils.traverseNode
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.zip.CRC32
import kotlin.coroutines.resume

class NodeHandler private constructor(private var service: Accessibility?) {

    companion object {
        fun bindTo(service: Accessibility): NodeHandler {
            return NodeHandler(service)
        }
    }

    fun unbind() {
        service = null
    }

    suspend fun findNodeByHash(hash: String): AccessibilityNodeInfo? {
        val root = service?.rootInActiveWindow ?: return null
        try {
            return suspendCancellableCoroutine { continuation ->
                traverseNode(root) { node ->
                    val currentHash = generateNodeHash(node)
                    if (currentHash == hash) {
                        if (!continuation.isCompleted)
                            continuation.resume(AccessibilityNodeInfo.obtain(node))
                        logE(TAG, "node 找到并返回, text: ${node.text.take(20)}")
                    }
                }

                if (!continuation.isCompleted) // 上面的递归是同步的
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

    /**
     * 根据节点生成字串用于生成哈希
     */
    private fun generateProperties(node: AccessibilityNodeInfo, parentHash: String?): String {
        val windowId = node.windowId.toString()
        val className = node.className.toString()
        val text = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        val parent = parentHash ?: ""
        return "$windowId|$className|$text|$contentDesc|$parent"
    }

    /**
     * 根据字符串生成哈希字串
     */
    private fun computeHash(properties: String): String {
        val crc32 = CRC32()
        crc32.update(properties.toByteArray())
        return crc32.value.toString(16).padStart(8, '0') // 8-digit hex
    }
}