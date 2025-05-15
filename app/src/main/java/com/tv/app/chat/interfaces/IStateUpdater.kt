package com.tv.app.chat.interfaces

import kotlin.reflect.KFunction1

/**
 * 其实也不是非得面向接口
 */
interface IStateUpdater<MSG, STATE, BUILDER> {
    fun setUpdateStateMethod(method: KFunction1<STATE.() -> STATE, Unit>)
    fun addMessage(message: MSG)
    fun updateMessage(
        which: MSG.() -> Boolean,
        update: BUILDER.() -> Unit
    )

    fun resetState()
}