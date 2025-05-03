package com.tv.app.settings.intances

import com.tv.app.settings.Setting
import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Names

class SystemPrompt : Setting<String>() {
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
        get() = false


    override fun onValidate(bean: Bean<String>): Result {
        return if (bean.value.isNotBlank()) {
            Result(true, null)
        } else {
            Result(false, "系统提示词不能为空")
        }
    }
} 