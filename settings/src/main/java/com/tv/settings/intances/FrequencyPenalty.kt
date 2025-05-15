package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class FrequencyPenalty : FloatSetting() {
    override val name: String
        get() = Names.FREQUENCY_PENALTY
    override val default: Bean<Float>
        get() = Bean(
            value = Default.FREQUENCY_PENALTY,
            isEnabled = false
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<Float>): Result {
        return if (bean.value in -2.0F..2.0F) {
            Result(true, null)
        } else {
            Result(false, "频率惩罚值必须在 -2 至 2 之间")
        }
    }
} 