package com.tv.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toPrettyJson

class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val nodeTree = ArrayList<Node>()
        if (event == null) {
            Log.d(TAG, "onAccessibilityEvent: event is null")
        } else {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                val packageName = event.packageName.toString()
                Log.d(TAG, packageName)
                try {
                    val rootNodeInfo = rootInActiveWindow
                    if (rootNodeInfo != null) {
                        with(rootNodeInfo){
                            val rect = Rect()
                            getBoundsInScreen(rect)
                            nodeTree.add(
                                Node(
                                    if (text == null) "null" else text.toString(),
                                    className.toString(),
                                    rect.left,
                                    rect.top,
                                    traverseNodeTree(this)
                                )
                            )
                        }
                        Log.d(TAG, nodeTree.toPrettyJson())
                    }
                } catch (e: Exception) {
                    e.logE(TAG)
                }
            }
        }
    }


    private fun traverseNodeTree(node: AccessibilityNodeInfo?) : List<Node>?{

        if (node==null) return null
        val childNodeTree = ArrayList<Node>()

        for (i in 0 until node.childCount) {
            node.getChild(i).run {
                val rect = Rect()
                getBoundsInScreen(rect)
                if (this != null) {
                    childNodeTree.add(
                        Node(
                            if(text == null) "null" else text.toString(),
                            className.toString(),
                            rect.left,
                            rect.top,
                            traverseNodeTree(node)
                        )
                    )
                }
            }
        }

        return childNodeTree
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected")
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "inUnBInd")
        return super.onUnbind(intent)
    }


}
