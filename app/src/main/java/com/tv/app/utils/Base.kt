package com.tv.app.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.tv.app.chat.ChatViewModel.Companion.CHAT_TAG
import com.tv.app.chat.mvi.bean.ChatMessage
import com.tv.app.chat.mvi.bean.systemMsg
import com.tv.app.settings.SettingsRepository
import com.tv.app.settings.values.Default
import com.zephyr.extension.mvi.MVIViewModel
import com.zephyr.log.logE
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

fun getSystemPromptMsg(): ChatMessage =
    systemMsg(SettingsRepository.systemPromptSetting.value ?: Default.SYSTEM_PROMPT)

fun logC(string: String, cut: Boolean = true) {
    logE(
        CHAT_TAG,
        if (cut) string.addReturnChars(40) else string
    )
}