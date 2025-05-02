package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class Temperature : Setting2<Float>() {
    override val name: String
        get() = "温度"
    override val default: Bean<Float>
        get() = Bean(
            value = Default.TEMPERATURE,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.USE_DIALOG


    override fun onValidate(bean: Bean<Float>): Result {
        return if (bean.value in 0.0F..2.0F) {
            Result(true, null)
        } else {
            Result(false, "温度值必须在0.0至2.0之间")
        }
    }
} 