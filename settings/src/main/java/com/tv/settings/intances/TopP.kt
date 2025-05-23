package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class TopP : FloatSetting() {
    override val name: String
        get() = Names.TOP_P
    override val default: Bean<Float>
        get() = Bean(
            value = Default.TOP_P,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<Float>): Result {
        return if (bean.value in 0.0F..1.0F) {
            Result(true, null)
        } else {
            Result(false, "top_p 值必须在 0.0 至 1.0 之间")
        }
    }
} 