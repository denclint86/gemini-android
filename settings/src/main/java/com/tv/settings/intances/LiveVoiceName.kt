package com.tv.settings.intances

import com.tv.settings.beans.Voice
import com.tv.settings.values.Default
import com.tv.settings.values.Names

class LiveVoiceName : StringSetting() {
    override val name: String
        get() = Names.LIVE_VOICE_NAME
    override val default: Bean<String>
        get() = Bean(
            value = Default.LIVE_VOICE_NAME,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_SELECT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<String>): Result {
        val voices = Voice.entries.toSet()
        val isContained = voices.any { it.string == bean.value }

        return if (isContained) {
            Result(true, null)
        } else {
            Result(false, "不支持的声音")
        }
    }
} 