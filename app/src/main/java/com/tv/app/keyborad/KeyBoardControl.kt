package com.tv.app.keyborad

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE

object KeyBoardControl {
    fun focusOn(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val inputManager =
            editText.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(editText, 0)
    }

    fun loseFocusOn(editText: EditText) {
        val inputManager =
            editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun loseFocusOnForce(activity: Activity) {
        if (activity.currentFocus == null) return
        val inputManager =
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?: return
        inputManager.hideSoftInputFromWindow(
            activity.currentFocus!!
                .windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}
