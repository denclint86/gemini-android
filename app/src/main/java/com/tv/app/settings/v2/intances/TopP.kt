package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class TopP : Setting2<Float>() {
    override val name: String
        get() = "top_p"
    override val default: Bean<Float>
        get() = Bean(
            value = Default.TOP_P,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.USE_DIALOG


    override fun onValidate(bean: Bean<Float>): Result {
        return if (bean.value in 0.0F..1.0F) {
            Result(true, null)
        } else {
            Result(false, "top_p 值必须在 0.0 至 1.0 之间")
        }
    }
} 