package com.tv.utils.keyborad

import android.graphics.Rect
import android.view.View

class HeightGetter(
    private val decorView: View
) : IKeyboardUtil.IHeightGetter {
    companion object {
        private const val KEYBOARD_VISIBLE_RATIO = 0.15
    }

    override fun get(): Int {
        val rect = Rect().apply {
            decorView.getWindowVisibleDisplayFrame(this)
        }

        // 计算屏幕和键盘高度
        val screenHeight = decorView.height
        val keyboardHeight = screenHeight - rect.bottom

        val realHeight =
            if (keyboardHeight < screenHeight * KEYBOARD_VISIBLE_RATIO) 0 else keyboardHeight

        return realHeight
    }
}