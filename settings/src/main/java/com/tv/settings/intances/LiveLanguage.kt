package com.tv.settings.intances

import com.tv.settings.beans.Language
import com.tv.settings.values.Default
import com.tv.settings.values.Names

class LiveLanguage : StringSetting() {
    override val name: String
        get() = Names.LIVE_LANGUAGE
    override val default: Bean<String>
        get() = Bean(
            value = Default.LIVE_LANGUAGE,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_SELECT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<String>): Result {
        val languages = Language.entries.toSet()
        val isContained = languages.any { it.string == bean.value }

        return if (isContained) {
            Result(true, null)
        } else {
            Result(false, "不支持的语言")
        }
    }
} 