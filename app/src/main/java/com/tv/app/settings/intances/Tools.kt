package com.tv.app.settings.intances

import com.tv.app.settings.Setting
import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names

class Tools : Setting<Boolean>() {
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

    override suspend fun set(bean: Bean<Boolean>): Result {
        return super.set(bean.copy(value = isEnabled))
    }
}