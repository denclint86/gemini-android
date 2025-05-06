package com.tv.app.viewmodel.chat.mvi

import com.zephyr.scaling_layout.State

sealed class ChatIntent {
    data class Chat(val text: String) : ChatIntent()
    data class SaveButtonState(val newState: State) : ChatIntent()
    data object ResetChat : ChatIntent()
    data object ReloadChat : ChatIntent()
}