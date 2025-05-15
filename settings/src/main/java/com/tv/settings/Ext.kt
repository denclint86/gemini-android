package com.tv.settings

import com.tv.settings.intances.BooleanSetting
import com.tv.settings.intances.DoubleSetting
import com.tv.settings.intances.FloatSetting
import com.tv.settings.intances.IntSetting
import com.tv.settings.intances.LongSetting
import com.tv.settings.intances.Setting
import com.tv.settings.intances.StringSetting
import com.tv.utils.castAs
import com.zephyr.global_values.TAG
import com.zephyr.log.logE

inline fun <reified T : Setting<*>> getSetting(): T? =
    SettingManager[T::class]

fun Setting<*>.getT(): Class<*> =
    when (this) {
        is BooleanSetting -> Boolean::class.java

        is DoubleSetting -> Double::class.java

        is FloatSetting -> Float::class.java

        is IntSetting -> Int::class.java

        is LongSetting -> Long::class.java

        is StringSetting -> String::class.java
    }

fun Setting<*>.parseFromAny(v: Any) =
    when (this) {
        is BooleanSetting -> {
            v.castAs { false }
        }

        is DoubleSetting -> v.castAs { (it as? Number)?.toDouble() }

        is FloatSetting -> v.castAs { (it as? Number)?.toFloat() }

        is IntSetting -> v.castAs {
            (it as? Number)?.toInt()
        }

        is LongSetting -> v.castAs {
            (it as? Number)?.toLong()
        }

        is StringSetting -> v.castAs<String> { it.toString() }
    }

@Suppress("UNCHECKED_CAST")
fun <T> Setting<*>.parseAsT(v: Any): T? {
    val value = if (v !is String)
        parseFromAny(v)
    else
        parseFromString(v)

    logE(TAG, "$v 解析为: $value[${(value ?: Any())::class.java.simpleName}]")
    return value as? T
}