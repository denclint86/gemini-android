package com.tv.app.settings.intances

import com.tv.app.settings.Setting
import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names

class Index : Setting<Long>() {
    override val name: String
        get() = Names.INDEX
    override val default: Bean<Long>
        get() = Bean(
            value = Default.INDEX,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.READ_ONLY
    override val canSetEnabled: Boolean
        get() = false


    override fun onValidate(bean: Bean<Long>): Result {
        return if (bean.value >= 0) {
            Result(true, null)
        } else {
            Result(false, "索引必须为非负数")
        }
    }
} 