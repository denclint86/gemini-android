package com.tv.app.chat.mvi

import com.zephyr.extension.mvi.MVIViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


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