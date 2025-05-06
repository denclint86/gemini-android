package com.tv.app.settings.intances

import com.tv.app.settings.FloatSetting
import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names

class PresencePenalty : FloatSetting() {
    override val name: String
        get() = Names.PRESENCE_PENALTY
    override val default: Bean<Float>
        get() = Bean(
            value = Default.PRESENCE_PENALTY,
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
            Result(false, "令牌惩罚值必须在 -2 至 2 之间")
        }
    }
}