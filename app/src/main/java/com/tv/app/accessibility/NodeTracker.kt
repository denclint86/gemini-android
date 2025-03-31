package com.tv.app.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import java.util.zip.CRC32

class NodeTracker(var service: MyAccessibilityService) {
    private val nodeHashMap = mutableMapOf<AccessibilityNodeInfo, String>()
    private val hashToNodeMap = mutableMapOf<String, AccessibilityNodeInfo>()

    fun updateNodeMapping() {
        nodeHashMap.clear()
        hashToNodeMap.clear()
        service.traverseNode(service.rootInActiveWindow) { node ->
            val hash = generateNodeHash(node)
            hashToNodeMap[hash] = node
        }
    }

    fun findNodeByHash(hash: String): AccessibilityNodeInfo? {
        return hashToNodeMap[hash]
    }

    fun getNodeHashMap(): Map<String, AccessibilityNodeInfo> {
        return hashToNodeMap.toMap()
    }

    fun generateNodeHash(node: AccessibilityNodeInfo): String {
        if (node in nodeHashMap) {
            return nodeHashMap[node]!!
        }
        val parent = node.getParent()
        val parentHash = parent?.let { generateNodeHash(it) } ?: ""
        val properties = generateProperties(node, parentHash)
        val hash = computeHash(properties)
        nodeHashMap[node] = hash
        return hash
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
        return crc32.value.toString(16).padStart(8, '0') // 8位十六进制
    }
}