package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class Tools : BooleanSetting() {
    override val name: String
        get() = Names.TOOLS
    override val default: Bean<Boolean>
        get() = Bean(
            value = Default.TOOLS,
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