package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class SleepTime : Setting2<Long>() {
    override val name: String
        get() = "调用冷却时间"
    override val default: Bean<Long>
        get() = Bean(
            value = Default.SLEEP_TIME,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.USE_DIALOG


    override fun onValidate(bean: Bean<Long>): Result {
        return if (bean.value >= 0) {
            Result(true, null)
        } else {
            Result(false, "休眠时间必须非负")
        }
    }
} 