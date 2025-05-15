package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

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
        get() = true


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value > 0) {
            Result(true, null)
        } else {
            Result(false, "此项必须为正数")
        }
    }
} 