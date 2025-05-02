package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class SystemPrompt : Setting2<String>() {
    override val name: String
        get() = "提示词"
    override val default: Bean<String>
        get() = Bean(
            value = Default.SYSTEM_PROMPT,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.USE_ACTIVITY


    override fun onValidate(bean: Bean<String>): Result {
        return if (bean.value.isNotBlank()) {
            Result(true, null)
        } else {
            Result(false, "系统提示词不能为空")
        }
    }
} 