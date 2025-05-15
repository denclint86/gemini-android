package com.tv.app.chat.mvi

sealed class ChatIntent {
    data class Chat(val text: String) : ChatIntent()
    data object ResetChat : ChatIntent()
    data object ReloadChat : ChatIntent()
    data object LogHistory : ChatIntent()
}