package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class Stream : BooleanSetting() {
    override val name: String
        get() = Names.STREAM
    override val default: Bean<Boolean>
        get() = Bean(
            value = Default.STREAM,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIRECT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<Boolean>): Result {
        return Result(true)
    }
}