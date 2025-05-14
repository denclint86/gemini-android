package com.tv.app.settings.intances

import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names

class Live : BooleanSetting() {
    override val name: String
        get() = Names.LIVE
    override val default: Bean<Boolean>
        get() = Bean(
            value = Default.LIVE,
            isEnabled = false
        )
    override val kind: Kind
        get() = Kind.DIRECT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<Boolean>): Result {
        return Result(true)
    }
}