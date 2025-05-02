package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class Index : Setting2<Int>() {
    override val name: String
        get() = "api_key 索引"
    override val default: Bean<Int>
        get() = Bean(
            value = Default.INDEX,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.READ_ONLY


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value >= 0) {
            Result(true, null)
        } else {
            Result(false, "索引必须为非负数")
        }
    }
} 