package com.tv.app.settings.intances

import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names

class ApiVersion : StringSetting() {
    override val name: String
        get() = Names.API_VERSION
    override val default: Bean<String>
        get() = Bean(
            value = Default.API_VERSION,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = false


    override fun onValidate(bean: Bean<String>): Result {
        return if (bean.value.isNotBlank()) {
            Result(true, null)
        } else {
            Result(false, "api 版本不能为空")
        }
    }
}