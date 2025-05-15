package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class Temperature : FloatSetting() {
    override val name: String
        get() = Names.TEMPERATURE
    override val default: Bean<Float>
        get() = Bean(
            value = Default.TEMPERATURE,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<Float>): Result {
        return if (bean.value in 0.0F..2.0F) {
            Result(true, null)
        } else {
            Result(false, "温度值必须在0.0至2.0之间")
        }
    }
} 