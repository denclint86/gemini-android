package com.tv.app.chat.mvi

sealed class ChatEffect {
    data object ChatSent : ChatEffect()
    data class Error(val t: Throwable?) : ChatEffect()
    data object Generating : ChatEffect()
}