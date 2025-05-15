package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class LivePrompt : StringSetting() {
    override val name: String
        get() = Names.LIVE_PROMPT
    override val default: Bean<String>
        get() = Bean(
            value = Default.LIVE_PROMPT,
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
            Result(false, "提示词不能为空")
        }
    }
} 