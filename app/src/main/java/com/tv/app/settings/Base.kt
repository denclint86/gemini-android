package com.tv.app.settings


abstract class StringSetting : Setting<String>() {
    override fun parseFromString(string: String): String? {
        return string // 直接返回字符串，空字符串也有效
    }
}

abstract class IntSetting : Setting<Int>() {
    override fun parseFromString(string: String): Int? {
        return string.toIntOrNull() // 解析为 Int，失败返回 null
    }
}

abstract class LongSetting : Setting<Long>() {
    override fun parseFromString(string: String): Long? {
        return string.toLongOrNull() // 解析为 Long
    }
}

abstract class FloatSetting : Setting<Float>() {
    override fun parseFromString(string: String): Float? {
        return string.toFloatOrNull() // 解析为 Float
    }
}

abstract class DoubleSetting : Setting<Double>() {
    override fun parseFromString(string: String): Double? {
        return string.toDoubleOrNull() // 解析为 Double
    }
}

abstract class BooleanSetting : Setting<Boolean>() {
    override fun parseFromString(string: String): Boolean? {
        return string.toBooleanStrictOrNull() // 严格解析 "true" 或 "false"
    }

    override suspend fun set(bean: Bean<Boolean>): Result {
        return super.set(bean.copy(value = bean.isEnabled))
    }

    override suspend fun get(): Bean<Boolean> {
        return super.get().copy(value = isEnabled())
    }
}