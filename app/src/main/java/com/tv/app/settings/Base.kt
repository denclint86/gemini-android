package com.tv.app.settings

import kotlin.math.roundToInt
import kotlin.math.roundToLong


abstract class StringSetting : Setting<String>() {
    override fun parseFromString(string: String): String? {
        return string // 直接返回字符串，空字符串也有效
    }
}

abstract class IntSetting : Setting<Int>() {
    override fun parseFromString(string: String): Int? {
        return string.toDoubleOrNull()?.roundToInt()
    }
}

abstract class LongSetting : Setting<Long>() {
    override fun parseFromString(string: String): Long? {
        return string.toDoubleOrNull()?.roundToLong()
    }
}

abstract class FloatSetting : Setting<Float>() {
    override fun parseFromString(string: String): Float? {
        return string.toFloatOrNull()
    }
}

abstract class DoubleSetting : Setting<Double>() {
    override fun parseFromString(string: String): Double? {
        return string.toDoubleOrNull()
    }
}

abstract class BooleanSetting : Setting<Boolean>() {
    override fun parseFromString(string: String): Boolean? {
        return string.toBooleanStrictOrNull()
    }

    // 强制绑定开关和 value 的值
    override suspend fun set(bean: Bean<Boolean>): Result {
        return super.set(bean.copy(value = bean.isEnabled))
    }

    override suspend fun get(): Bean<Boolean> {
        return super.get().copy(value = isEnabled())
    }
}