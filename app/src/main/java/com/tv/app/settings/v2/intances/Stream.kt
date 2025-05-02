package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class Stream : Setting2<Boolean>() {
    override val name: String
        get() = "使用流式传输"
    override val default: Bean<Boolean>
        get() = Bean(
            value = Default.STREAM,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_SELECT


    override fun onValidate(bean: Bean<Boolean>): Result {
        return Result(true)
    }
}