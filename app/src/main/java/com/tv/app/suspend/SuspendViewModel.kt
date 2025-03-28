package com.tv.app.suspend

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object SuspendViewModel : ViewModel() {
    // 控制普通Service悬浮窗的显示和隐藏
    var isShowSuspendWindow = MutableLiveData<Boolean>()
}