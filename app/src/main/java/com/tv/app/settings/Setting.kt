package com.tv.app.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.annotations.SerializedName
import com.tv.app.utils.toJsonClass
import com.zephyr.datastore.getValue
import com.zephyr.datastore.putValue
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt


abstract class Setting<T> {
    companion object {
        const val NAME_KEY = "setting_key"
        const val RESULT_NAME_KEY = "setting_result_key"
    }

    abstract val name: String
    abstract val default: Bean<T>
    abstract val kind: Kind // 给 view 层做适配用的, 并没有实质性的限制

    abstract val canSetEnabled: Boolean

    private val key = stringPreferencesKey("setting_${name}")

    lateinit var preview: Bean<T>
        private set

    init {
        runBlocking {
            get() // 会初始化 preview
        }
    }

    var value: T?
        get() = if (preview.isEnabled) preview.value else null
        set(newValue) {
            if (newValue == null) return
            runBlocking {
                set {
                    value = newValue
                }
            }
        }

    var isEnabled: Boolean
        get() = preview.isEnabled
        set(newEnabled) {
            runBlocking {
                set {
                    isEnabled = newEnabled
                }
            }
        }

    suspend fun set(str: String, isEnabled: Boolean = preview.isEnabled): Result {
        val t = parseAsT(str)
            ?: return Result(false, "parse as? t failed")
        val bean = Bean(t, isEnabled)
        return set(bean)
    }

    suspend fun set(block: Builder<T>.() -> Unit): Result {
        val builder = Builder(preview)
        builder.block()
        val bean = Bean(builder.value, builder.isEnabled)
        return set(bean)
    }

    open suspend fun set(bean: Bean<T>): Result {
        if (!canSetEnabled && preview.isEnabled != bean.isEnabled) return Result(
            false,
            "this attr can't edit 'isEnabled'"
        )
        val validateResult = onValidate(bean)

        if (validateResult.isSuccess) {
            key.putValue(bean.toJson())
        }

        return validateResult
            .also {
                if (it.isSuccess)
                    preview = bean
            }
    }

    suspend fun get(): Bean<T> {
        val json = key.getValue("")
        logE(TAG, json)

        return if (json.isNotEmpty()) {
            json.toJsonClass<Bean<T>>()!!
        } else {
            default
        }.also { preview = it }
    }

    private fun parseAsT(str: String): T? {
        return try {
            value?.let { it::class.simpleName?.let { name -> logE(TAG, name) } }
            @Suppress("UNCHECKED_CAST")
            when (value) {
                is String -> str as? T
                is Int -> {
//                    str.toInt() as? T
                    (str.toIntOrNull() ?: str.toDouble().roundToInt()) as? T
                }

                is Boolean -> str.toBoolean() as? T
                is Float -> str.toFloat() as? T
                is Double -> str.toDouble() as? T
                is Long -> str.toLong() as? T
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    abstract fun onValidate(bean: Bean<T>): Result

    data class Result(
        val isSuccess: Boolean = true,
        val msg: String? = null
    )

    data class Bean<out T>(
        @SerializedName("value") val value: T,
        @SerializedName("is_enabled") val isEnabled: Boolean = true
    )

    enum class Kind {
        READ_ONLY,
        DIALOG_EDIT,
        DIALOG_SELECT,
        DIRECT,
        ACTIVITY
    }

    class Builder<T>(defaultBean: Bean<T>) {
        @JvmField
        var value = defaultBean.value

        @JvmField
        var isEnabled = defaultBean.isEnabled
    }

    override fun equals(other: Any?): Boolean {
        if (other is Setting<*>)
            return preview == other.preview
        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + preview.hashCode()
        return result
    }
}