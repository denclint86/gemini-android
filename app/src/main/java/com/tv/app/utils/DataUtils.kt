package com.tv.app.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tv.app.settings.BooleanSetting
import com.tv.app.settings.DoubleSetting
import com.tv.app.settings.FloatSetting
import com.tv.app.settings.IntSetting
import com.tv.app.settings.LongSetting
import com.tv.app.settings.Setting
import com.tv.app.settings.StringSetting
import com.zephyr.global_values.TAG
import com.zephyr.log.logE


inline fun <reified T> Any.castAs(onCast: (Any) -> T?): T? =
    if (this !is T) {
        logE(TAG, "${this::class.java.simpleName} 不是 ${T::class.java.simpleName}")
        onCast(this)
    } else {
        this
    }

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

fun String.addReturnChars(maxLength: Int): String {
    if (this.length <= maxLength || maxLength <= 0) return this

    val result = StringBuilder()
    var currentIndex = 0

    while (currentIndex < this.length) {
        // 计算剩余长度
        val remainingLength = this.length - currentIndex
        if (remainingLength <= maxLength) {
            result.append(this.substring(currentIndex))
            break
        }

        // 检查自然换行符
        val nextNewline = this.indexOf('\n', currentIndex)
        if (nextNewline != -1 && nextNewline - currentIndex <= maxLength) {
            result.append(this.substring(currentIndex, nextNewline + 1))
            currentIndex = nextNewline + 1
            continue
        }

        // 没有自然换行符时，找合适的断点
        var endIndex = currentIndex + maxLength
        if (endIndex >= this.length) {
            endIndex = this.length
        } else {
            // 回退到最后一个空格（如果有）
            val chunk = this.substring(currentIndex, endIndex)
            val lastSpace = chunk.lastIndexOf(' ')
            if (lastSpace > maxLength / 2) {
                endIndex = currentIndex + lastSpace
            }
        }

        result.append(this.substring(currentIndex, endIndex))
        if (endIndex < this.length) {
            result.append("\n")
        }
        currentIndex = endIndex
        // 跳过可能的空格
        while (currentIndex < this.length && this[currentIndex] == ' ') {
            currentIndex++
        }
    }

    return result.toString()
}

// 全局 Gson 实例，复用以提高性能
val gson: Gson = GsonBuilder()
//    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    .create()

// 扩展函数，保留 reified 泛型
inline fun <reified T> String.toJsonClass(): T? {
    return try {
        gson.fromJson(this, T::class.java)
    } catch (e: Exception) {
        null
    }
}