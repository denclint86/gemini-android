package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class PresencePenalty : Setting2<Float>() {
    override val name: String
        get() = "令牌惩罚"
    override val default: Bean<Float>
        get() = Bean(
            value = Default.PRESENCE_PENALTY,
            enabled = false
        )
    override val kind: Kind
        get() = Kind.USE_DIALOG


    override fun onValidate(bean: Bean<Float>): Result {
        return if (bean.value in -2.0F..2.0F) {
            Result(true, null)
        } else {
            Result(false, "令牌惩罚值必须在 -2 至 2 之间")
        }
    }
}