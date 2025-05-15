package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class SystemPrompt : StringSetting() {
    override val name: String
        get() = Names.SYSTEM_PROMPT
    override val default: Bean<String>
        get() = Bean(
            value = Default.SYSTEM_PROMPT,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.ACTIVITY
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<String>): Result {
        return if (bean.value.isNotBlank()) {
            Result(true, null)
        } else {
            Result(false, "系统提示词不能为空")
        }
    }
} 