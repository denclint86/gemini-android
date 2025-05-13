package com.tv.app.utils.keyborad

import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class FocusHandler(
    private val imm: InputMethodManager,
    private val editText: EditText,
    private val activity: AppCompatActivity
) : IKeyboardUtil.IFocusHandler {

    override fun focus() {
        editText.requestFocus()
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun loseFocus() {
        imm.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        editText.clearFocus()
    }

    override fun loseFocusForce() {
        activity.currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(
                view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
            editText.clearFocus()
        }
    }
}