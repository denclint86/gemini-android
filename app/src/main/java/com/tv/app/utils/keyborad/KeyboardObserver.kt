package com.tv.app.utils.keyborad

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class KeyboardObserver private constructor(private val activity: AppCompatActivity) {
    companion object {
        // 更可靠的阈值计算方式（屏幕高度的15%）
        private const val KEYBOARD_VISIBLE_RATIO = 0.15

        fun attach(activity: AppCompatActivity): KeyboardObserver {
            return KeyboardObserver(activity)
        }
    }

    enum class State {
        Visible, Hidden
    }

    private val _keyboardState = MutableLiveData(State.Hidden)
    private val _keyboardHeight = MutableLiveData(0)
    private val _keyboardLastShownTime = MutableLiveData(0L)

    val keyboardState: LiveData<State> get() = _keyboardState
    val keyboardHeight: LiveData<Int> get() = _keyboardHeight
    val keyboardLastShownTime: LiveData<Long> get() = _keyboardLastShownTime

    private val decorView: View = activity.window.decorView

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        checkKeyboardState()
    }

    init {
        decorView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    fun detach() {
        _keyboardState.removeObservers(activity)
        _keyboardHeight.removeObservers(activity)
        _keyboardLastShownTime.removeObservers(activity)
        decorView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun checkKeyboardState() {
        val rect = Rect().apply {
            decorView.getWindowVisibleDisplayFrame(this)
        }

        // 更精确的屏幕高度计算
        val screenHeight = decorView.height
        val keyboardHeight = screenHeight - rect.bottom

        val realHeight =
            if (keyboardHeight < screenHeight * KEYBOARD_VISIBLE_RATIO) 0 else keyboardHeight

        // 更新状态
        if (realHeight != 0) {
            _keyboardState.setIfChange(State.Visible)
        } else {
            _keyboardState.setIfChange(State.Hidden)
        }
        _keyboardHeight.setIfChange(realHeight)
        _keyboardLastShownTime.setIfChange(System.currentTimeMillis())
    }

    private fun <T> MutableLiveData<T>.setIfChange(t: T) {
        if (value != t)
            value = t
    }
}