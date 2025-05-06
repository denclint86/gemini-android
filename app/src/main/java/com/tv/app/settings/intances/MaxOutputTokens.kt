package com.tv.app.settings.intances

import com.tv.app.settings.IntSetting
import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names

class MaxOutputTokens : IntSetting() {
    override val name: String
        get() = Names.MAX_OUTPUT_TOKENS
    override val default: Bean<Int>
        get() = Bean(
            value = Default.MAX_OUTPUT_TOKENS,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = false


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value > 0) {
            Result(true, null)
        } else {
            Result(false, "此项必须为正数")
        }
    }
} 