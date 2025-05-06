package com.tv.app.view.ui.suspend

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.tv.app.App
import com.tv.app.utils.Role
import com.tv.app.viewmodel.chat.mvi.bean.ChatMessage

object SuspendLiveDataManager {
    // 控制普通Service悬浮窗的显示和隐藏
    val isShowSuspendWindow = MutableLiveData(true)

    // 悬浮窗显示的文本
    val suspendText = MutableLiveData("bot")

    fun update(last: ChatMessage) {
        val v = when {
            last.role == Role.SYSTEM -> "未开始聊天"
            last.role == Role.MODEL && last.text.isNotBlank() -> {
                App.binder.get()?.setProgressBarVisibility(View.INVISIBLE)
                last.text.take(4) + "..."
            }

            else -> {
                App.binder.get()?.setProgressBarVisibility(View.VISIBLE)
                "正在生成"
            }
        }
        suspendText.value = v
    }
}