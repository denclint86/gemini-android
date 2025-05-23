package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class Index : IntSetting() {
    override val name: String
        get() = Names.INDEX
    override val default: Bean<Int>
        get() = Bean(
            value = Default.INDEX,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.READ_ONLY
    override val canSetEnabled: Boolean
        get() = false


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value >= 0) {
            Result(true, null)
        } else {
            Result(false, "索引必须为非负数")
        }
    }
}
