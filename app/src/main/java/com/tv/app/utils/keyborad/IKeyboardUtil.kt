package com.tv.app.utils.keyborad

import androidx.lifecycle.LiveData

interface IKeyboardUtil {
    val state: LiveData<State>
    val height: LiveData<Int>

    fun getFocusHandler(): IFocusHandler

    fun detach()

    sealed class State(val name: String) {
        data class SoftKeyboard(val height: Int) : State("软键盘")

        data object OtherKeyboard : State("非软键盘")

        data object Hidden : State("键盘未激活")
    }

    interface IFocusHandler {
        fun focus()
        fun loseFocus()
        fun loseFocusForce()
    }

    interface IHeightGetter {
        fun get(): Int
    }
}