package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class SleepTime : LongSetting() {
    override val name: String
        get() = Names.SLEEP_TIME
    override val default: Bean<Long>
        get() = Bean(
            value = Default.SLEEP_TIME,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = false


    override fun onValidate(bean: Bean<Long>): Result {
        return if (bean.value >= 0) {
            Result(true, null)
        } else {
            Result(false, "休眠时间必须非负")
        }
    }
} 