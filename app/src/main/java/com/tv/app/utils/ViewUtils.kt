package com.tv.app.utils

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.MaterialToolbar
import com.tv.app.R
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.scaling_layout.ScalingLayout
import com.zephyr.scaling_layout.State


fun MaterialToolbar.setBackAffair() {
    setNavigationIcon(R.drawable.ic_back) // 替换为你的图标资源
    setNavigationOnClickListener {
        (context as? Activity)?.finish() // 点击事件，比如关闭 Activity
    }
}

fun View.parentIs(parentView: View): Boolean {
    if (this == parentView) return true

    var currentParent = parent
    while (currentParent != null) {
        if (currentParent == parentView) {
            return true
        }
        currentParent = (currentParent as? View)?.parent
    }
    return false
}

fun ScalingLayout.safeExpand(): Boolean {
    logE(TAG, "isAttachedToWindow: $isAttachedToWindow")
    logE(TAG, "windowVisibility: ${windowVisibility == View.VISIBLE}")
    if (isAttachedToWindow && windowVisibility == View.VISIBLE && state == State.COLLAPSED) {
        expand()
        return true
    } else {
        return false
    }
}

fun ScalingLayout.safeCollapse(): Boolean {
    logE(TAG, "isAttachedToWindow: $isAttachedToWindow")
    logE(TAG, "windowVisibility: ${windowVisibility == View.VISIBLE}")
    if (isAttachedToWindow && windowVisibility == View.VISIBLE && state == State.EXPANDED) {
        collapse()
        return true
    } else {
        return false
    }
}

fun View.setBackgroundColorFromAttr(@AttrRes attrResId: Int) {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attrResId, typedValue, true)

    // 如果属性是引用类型（如颜色资源引用）
    if (typedValue.resourceId != 0) {
        setBackgroundColor(ContextCompat.getColor(context, typedValue.resourceId))
    }
    // 如果属性直接是颜色值
    else if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
        typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT
    ) {
        setBackgroundColor(typedValue.data)
    }
}

fun TextView.setTextColorFromAttr(@AttrRes attrResId: Int) {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attrResId, typedValue, true)

    // 如果属性是引用类型（如颜色引用）
    if (typedValue.resourceId != 0) {
        setTextColor(ContextCompat.getColor(context, typedValue.resourceId))
    }
    // 如果属性直接是颜色值
    else if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
        typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT
    ) {
        setTextColor(typedValue.data)
    }
}

fun View.setRippleEffect(enabled: Boolean) {
    background = if (enabled) {
        // 启用涟漪效果
        TypedValue().let { outValue ->
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
            )
            ContextCompat.getDrawable(context, outValue.resourceId)
        }
    } else {
        null
    }
}

fun View.setViewInsets(block: ViewGroup.MarginLayoutParams.(Insets) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            block(insets) // 将 insets 传递给 block
        }
        WindowInsetsCompat.CONSUMED
    }
}