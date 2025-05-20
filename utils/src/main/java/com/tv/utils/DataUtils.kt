package com.tv.utils

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

fun CharSequence?.contains(set: Set<String>): Boolean {
    if (this == null) return false

    return set.any { value ->
        this.contains(value, ignoreCase = true)
    }
}

inline fun <reified T> Any.castAs(onCast: (Any) -> T?): T? =
    if (this !is T) {
//        logE(TAG, "${this::class.java.simpleName} 不是 ${T::class.java.simpleName}")
        onCast(this)
    } else {
        this
    }


fun <T> MutableLiveData<T>.setIfChange(t: T): Boolean {
    val diff = value != t
    if (diff)
        value = t
    return diff
}

fun <T : Any> KClass<T>.createInstanceOrNull(): T? = try {
    if (isAbstract)
        null
    else
        createInstance()
} catch (t: Throwable) {
    null
}

inline fun <reified T : Any> getSealedChildren(
    noinline filter: (KClass<out T>) -> T?
): List<T> {
    return getSealedChildren(T::class, filter)
}

fun <T : Any> getSealedChildren(
    sealedClass: KClass<T>,
    filter: (KClass<out T>) -> T?
): List<T> {
    require(sealedClass.isSealed) { "传入的参数必须是封装类" }

    // 递归收集所有子类的函数
    fun collectSubclasses(kClass: KClass<out T>): List<KClass<out T>> {
        return if (kClass.isSealed) {
            // 如果是密封类, 递归收集其直接子类的子类
            kClass.sealedSubclasses.flatMap { collectSubclasses(it) }
        } else {
            // 如果不是密封类, 直接返回自身
            listOf(kClass)
        }
    }

    // 获取所有子类 (包括间接子类), 然后应用 filter
    return collectSubclasses(sealedClass).mapNotNull(filter)
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

        // 没有自然换行符时, 找合适的断点
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

// 全局 Gson 实例, 复用以提高性能
val gson: Gson = GsonBuilder()
//    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    .create()

// 扩展函数, 保留 reified 泛型
inline fun <reified T> String.toJsonClass(): T? {
    return try {
        gson.fromJson(this, T::class.java)
    } catch (e: Exception) {
        null
    }
}