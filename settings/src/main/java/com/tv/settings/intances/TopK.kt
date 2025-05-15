package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class TopK : IntSetting() {
    override val name: String
        get() = Names.TOP_K
    override val default: Bean<Int>
        get() = Bean(
            value = Default.TOP_K,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value in 1..100) {
            Result(true, null)
        } else {
            Result(false, "top_k 值必须在 1 至 100 之间")
        }
    }
} 