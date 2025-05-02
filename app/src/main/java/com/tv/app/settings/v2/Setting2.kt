package com.tv.app.settings.v2

import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.annotations.SerializedName
import com.zephyr.datastore.getValue
import com.zephyr.datastore.putValue
import com.zephyr.net.toJson
import com.zephyr.net.toJsonClass
import kotlinx.coroutines.runBlocking

abstract class Setting2<T : Any> {
    abstract val name: String
    abstract val default: Bean<T>
    abstract val kind: Kind // 给 view 层做适配用的, 并没有实质性的限制

    private val key = stringPreferencesKey("setting_${name}")

    lateinit var preview: Bean<T>
        private set

    init {
        runBlocking {
            get() // 会初始化 preview
        }
    }

    var value: T?
        get() = if (preview.enabled) preview.value else null
        set(newValue) {
            if (newValue == null) return
            runBlocking {
                set {
                    value = newValue
                }
            }
        }

    var isEnabled: Boolean
        get() = preview.enabled
        set(newEnabled) {
            runBlocking {
                set {
                    isEnabled = newEnabled
                }
            }
        }

    suspend fun set(block: Builder<T>.() -> Unit): Result {
        val builder = Builder(preview)
        builder.block()
        val bean = Bean(builder.value, builder.isEnabled)
        return set(bean)
    }

    suspend fun set(bean: Bean<T>): Result {
        // 完全不能修改也不可行
//        if (kind == Kind.READ_ONLY) throw IllegalStateException("尝试修改不允许修改的配置")

        val validateResult = onValidate(bean)

        if (validateResult.isSuccess) {
            key.putValue(bean.toJson())
        }

        return validateResult
    }

    suspend fun get(): Bean<T> {
        val json = key.getValue("")

        return if (json.isNotEmpty()) {
            json.toJsonClass<Bean<T>>()!!
        } else {
            default
        }.also { preview = it }
    }

    abstract fun onValidate(bean: Bean<T>): Result

    data class Result(
        val isSuccess: Boolean = true,
        val msg: String? = null
    )

    data class Bean<out T : Any>(
        @SerializedName("value") val value: T,
        @SerializedName("enabled") val enabled: Boolean = true
    )

    enum class Kind {
        READ_ONLY,
        DIALOG_EDIT,
        DIALOG_SELECT,
        ACTIVITY
    }

    class Builder<T : Any>(defaultBean: Bean<T>) {
        @JvmField
        var value = defaultBean.value

        @JvmField
        var isEnabled = defaultBean.enabled
    }


    override fun equals(other: Any?): Boolean {
        if (other is Setting2<*>)
            return preview == other.preview
        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + preview.hashCode()
        return result
    }
}