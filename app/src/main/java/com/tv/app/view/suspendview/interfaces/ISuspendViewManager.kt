package com.tv.app.view.suspendview.interfaces

interface ISuspendViewManager {
    var rootVisibility: Int
    var progressBarVisibility: Int
//    var text: String

    //    fun observeTo(liveData: LiveData<String>)
    fun release()
    fun setOnTouchEventListener(l: SuspendViewEventCallback?)
}