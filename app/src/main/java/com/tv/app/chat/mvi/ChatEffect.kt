package com.tv.app.chat.mvi

sealed class ChatEffect {
    data class Error(val t: Throwable?) : ChatEffect()
}