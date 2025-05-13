package com.tv.app.utils.keyborad

import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tv.app.utils.setIfChange
import com.tv.app.utils.withLifecycleScope
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class KeyboardUtil private constructor(
    private val editText: EditText,
    private val activity: AppCompatActivity
) : IKeyboardUtil {
    companion object {
        fun attach(editText: EditText, activity: AppCompatActivity): IKeyboardUtil {
            return KeyboardUtil(editText, activity)
        }
    }

    private val imm: InputMethodManager =
        activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager

    private val decorView: View = activity.window.decorView

    private val _state = MutableLiveData<IKeyboardUtil.State>(IKeyboardUtil.State.Hidden)
    override val state: LiveData<IKeyboardUtil.State> = _state

    private val _height = MutableLiveData(0)
    override val height: LiveData<Int> = _height

    private var job: Job? = null

    private val iFocusHandler: IKeyboardUtil.IFocusHandler by lazy {
        FocusHandler(
            imm,
            editText,
            activity
        )
    }

    private val iHeightGetter: IKeyboardUtil.IHeightGetter by lazy {
        HeightGetter(decorView)
    }

    private val listener1 = ViewTreeObserver.OnGlobalLayoutListener {
        val h = iHeightGetter.get()

        if (_height.setIfChange(h))
            logE(TAG, "新键盘高度: $h")
    }

    private val listener2 = View.OnFocusChangeListener { v, hasFocus ->
        job?.cancel()
        job = activity.withLifecycleScope(Dispatchers.Main) {
            val isActive = (hasFocus && imm.isActive(v))
            delay(100) // 延迟等待键盘展开
            val height = height.value ?: 0
            val newState = when {
                isActive && height != 0 -> IKeyboardUtil.State.SoftKeyboard(height)
                isActive && height == 0 -> IKeyboardUtil.State.OtherKeyboard
                else -> IKeyboardUtil.State.Hidden
            }

            if (_state.setIfChange(newState))
                logE(TAG, "键盘状态: ${newState.name}")
        }
    }

    init {
        decorView.viewTreeObserver.addOnGlobalLayoutListener(listener1)
        editText.onFocusChangeListener = listener2
    }

    override fun getFocusHandler(): IKeyboardUtil.IFocusHandler = iFocusHandler

    override fun detach() {
        _state.removeObservers(activity)
        _height.removeObservers(activity)
        decorView.viewTreeObserver.removeOnGlobalLayoutListener(listener1)
        editText.onFocusChangeListener = null
    }
}