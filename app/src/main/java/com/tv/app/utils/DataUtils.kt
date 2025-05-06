package com.tv.app.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.zephyr.log.logE

// 勉强解决
fun parseFromNumber(num: Number, clazz: Class<*>): Number? {
    return when {
        clazz.name.contains("Integer") -> {
            num.toInt()
        }

        clazz.name.contains("Long") -> {
            num.toLong()
        }

        clazz.name.contains("Float") -> {
            num.toFloat()
        }

        clazz.name.contains("Double") -> {
            num.toDouble()
        }

        else -> {
            null
        }
    }
}


@Suppress("UNCHECKED_CAST")
fun <T> parseFromNumber1(num: Number, clazz: Class<out T>): T? {
    return when (clazz) {
        Int::class.java -> when (num) {
            is Int -> num as T
            is Long -> if (num in Int.MIN_VALUE..Int.MAX_VALUE) num.toInt() as T else null
            is Float -> if (num.isFinite() && num == num.toInt()
                    .toFloat()
            ) num.toInt() as T else null

            is Double -> if (num.isFinite() && num == num.toInt()
                    .toDouble()
            ) num.toInt() as T else null

            else -> null
        }

        Long::class.java -> when (num) {
            is Int -> num.toLong() as T
            is Long -> num as T
            is Float -> if (num.isFinite() && num == num.toLong()
                    .toFloat()
            ) num.toLong() as T else null

            is Double -> if (num.isFinite() && num == num.toLong()
                    .toDouble()
            ) num.toLong() as T else null

            else -> null
        }

        Float::class.java -> when (num) {
            is Int -> num.toFloat() as T
            is Long -> num.toFloat() as T
            is Float -> if (num.isFinite()) num as T else null
            is Double -> if (num.isFinite() && num >= -Float.MAX_VALUE && num <= Float.MAX_VALUE) num.toFloat() as T else null
            else -> null
        }

        Double::class.java -> when (num) {
            is Int -> num.toDouble() as T
            is Long -> num.toDouble() as T
            is Float -> num.toDouble() as T
            is Double -> if (num.isFinite()) num as T else null
            else -> null
        }

        else -> {
            logE("", clazz.simpleName)
            null // 不支持的类型
        }
    }
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
    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    .create()

// 扩展函数，保留 reified 泛型
inline fun <reified T> String.toJsonClass(): T? {
    return try {
        gson.fromJson(this, T::class.java)
    } catch (e: Exception) {
        null
    }
}