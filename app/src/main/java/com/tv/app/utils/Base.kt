package com.tv.app.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.tv.app.model.SettingsRepository
import com.tv.app.settings.intances.SystemPrompt
import com.tv.app.settings.values.Default
import com.tv.app.viewmodel.chat.mvi.bean.ChatMessage
import com.tv.app.viewmodel.chat.mvi.bean.systemMsg
import com.zephyr.extension.mvi.MVIViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val alwaysActiveLifecycleOwner = object : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}

fun <T, R> MVIViewModel<*, T, *>.observe(
    scope: CoroutineScope,
    filter: suspend (T) -> R,
    action: suspend (R) -> Unit
) {
    observeState {
        scope.launch {
            map(filter).collect(action)
        }
    }
}

suspend fun <T> MVIViewModel<*, *, T>.collectFlow(action: suspend (T) -> Unit) {
    uiEffectFlow.collect(action)
}

val testMsgList: List<ChatMessage> = listOf(
    ChatMessage(text = "SSSSaaaaa\nbbbbb\nccccc\nddddd", role = Role.SYSTEM),
    ChatMessage(text = "UUUUaaaaa\nbbbbb\nccccc\nddddd", role = Role.USER),
    ChatMessage(text = "MMMMaaaaa\nbbbbb\nccccc\nddddd", role = Role.MODEL),
    ChatMessage(text = "FFFFaaaaa\nbbbbb\nccccc\nddddd", role = Role.FUNC),
    ChatMessage(text = "MMMMaaaaa\nbbbbb\nccccc\nddddd", role = Role.MODEL)
)

val systemMsg: ChatMessage =
    systemMsg(SettingsRepository.get<SystemPrompt>()?.value(true) ?: Default.SYSTEM_PROMPT)

val systemMsgList: List<ChatMessage> = listOf(systemMsg)
