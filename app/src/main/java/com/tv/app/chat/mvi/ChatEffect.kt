package com.tv.app.chat.mvi

sealed class ChatEffect {
    data class ChatSent(val shouldClear: Boolean) : ChatEffect()
    data object Done : ChatEffect()
    data class Error(val t: Throwable?) : ChatEffect()
    data object Generating : ChatEffect()
}