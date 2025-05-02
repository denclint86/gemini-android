package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class MaxOutputTokens : Setting2<Int>() {
    override val name: String
        get() = "最大输出 tokens"
    override val default: Bean<Int>
        get() = Bean(
            value = Default.MAX_OUTPUT_TOKENS,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value > 0) {
            Result(true, null)
        } else {
            Result(false, "此项必须为正数")
        }
    }
} 