package com.tv.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zephyr.extension.widget.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun <reified T : ViewModel> ViewModelStoreOwner.createViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
//    return ViewModelProvider(this).get(T::class.java) // 重写 [] get 操作符来实现语法简化
}

inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    val i = Intent(this, T::class.java)
    i.block()
    startActivity(i)
}

fun Activity.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
    "已复制到剪贴板".toast()
}

fun createLayoutParam(x: Int = 0, y: Int = 0) = WindowManager.LayoutParams().apply {
    type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    format = PixelFormat.RGBA_8888
    flags =
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    width = WindowManager.LayoutParams.WRAP_CONTENT
    height = WindowManager.LayoutParams.WRAP_CONTENT
    gravity = Gravity.START or Gravity.TOP

    this.x = x
    this.y = y
}

fun Activity.findViewAtPoint(x: Float, y: Float): View? {
    // 遍历 View 树, 找到点击位置的 View
    val rootView = window.decorView
    return findViewAtPointRecursive(rootView, x, y)
}

private fun findViewAtPointRecursive(view: View, x: Float, y: Float): View? {
    if (view !is ViewGroup) {
        return if (view.isClickable && isPointInsideView(x, y, view)) view else null
    }
    // 从上到下遍历子 View
    for (i in view.childCount - 1 downTo 0) {
        val child = view.getChildAt(i)
        if (isPointInsideView(x, y, child)) {
            return findViewAtPointRecursive(child, x, y) ?: child
        }
    }
    return if (view.isClickable && isPointInsideView(x, y, view)) view else null
}

private fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    val left = location[0]
    val top = location[1]
    val right = left + view.width
    val bottom = top + view.height
    return x >= left && x <= right && y >= top && y <= bottom && view.isShown
}

fun Context.withLifecycleScope(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job? {
    return (this as? LifecycleOwner)?.lifecycleScope?.launch(context, start, block)
}

fun Context.showInputDialog(
    title: String = "",
    msg: String = "",
    initText: String = "",
    onDismiss: ((String?) -> Unit) = {},
) {
    val editText = EditText(this)

    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(msg)
        .setView(editText)
        .setCancelable(true)
        .setPositiveButton("确认") { _, _ ->
            onDismiss(editText.text.toString())
        }
        .setNegativeButton("取消") { _, _ ->
            onDismiss(null)
        }
        .setOnCancelListener {
            onDismiss(null)
        }.create().show()

    editText.setText(initText)
}

fun Context.showSingleChoiceDialog(
    title: String = "",
    items: List<String>,
    selectedIndex: Int = 0,
    onDismiss: ((String?) -> Unit) = {}
) {
    // 防止传入的索引超出范围
    val initialSelection = when {
        items.isEmpty() -> -1
        selectedIndex < 0 -> 0
        selectedIndex >= items.size -> 0
        else -> selectedIndex
    }

    // 用于记录当前选中项的索引
    var checkedItem = initialSelection

    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setSingleChoiceItems(items.toTypedArray(), initialSelection) { _, which ->
            checkedItem = which
        }
        .setCancelable(true)
        .setPositiveButton("确认") { _, _ ->
            if (checkedItem != -1 && checkedItem < items.size) {
                onDismiss(items[checkedItem])
            } else {
                onDismiss(null)
            }
        }
        .setNegativeButton("取消") { _, _ ->
            onDismiss(null)
        }
        .setOnCancelListener {
            onDismiss(null)
        }
        .create()
        .show()
}