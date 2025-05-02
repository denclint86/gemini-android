package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class TopK : Setting2<Int>() {
    override val name: String
        get() = "top_k"
    override val default: Bean<Int>
        get() = Bean(
            value = Default.TOP_K,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value in 1..100) {
            Result(true, null)
        } else {
            Result(false, "top_k 值必须在 1 至 100 之间")
        }
    }
} 