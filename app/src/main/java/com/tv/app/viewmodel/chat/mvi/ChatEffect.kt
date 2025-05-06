package com.tv.app.viewmodel.chat.mvi

sealed class ChatEffect {
    data class ChatSent(val shouldClear: Boolean) : ChatEffect()
    data class Error(val t: Throwable?) : ChatEffect()
    data object Generating : ChatEffect()
}