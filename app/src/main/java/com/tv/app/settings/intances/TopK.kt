package com.tv.app.settings.intances

import com.tv.app.settings.Setting
import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names

class TopK : Setting<Long>() {
    override val name: String
        get() = Names.TOP_K
    override val default: Bean<Long>
        get() = Bean(
            value = Default.TOP_K,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<Long>): Result {
        return if (bean.value in 1..100) {
            Result(true, null)
        } else {
            Result(false, "top_k 值必须在 1 至 100 之间")
        }
    }
} 