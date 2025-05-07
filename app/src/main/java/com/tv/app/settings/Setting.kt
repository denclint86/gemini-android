package com.tv.app.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.annotations.SerializedName
import com.tv.app.utils.parseFromNumber
import com.tv.app.utils.toJsonClass
import com.zephyr.datastore.getValue
import com.zephyr.datastore.putValue
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import kotlinx.coroutines.runBlocking


abstract class Setting<T> {
    companion object {
        const val NAME_KEY = "setting_key"
        const val RESULT_NAME_KEY = "setting_result_key"
    }

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

    abstract val name: String
    abstract val kind: Kind // 给 view 层做适配用的, 并没有实质性的限制

    protected abstract val default: Bean<T>
    abstract val canSetEnabled: Boolean

    private val key = stringPreferencesKey("setting_${name}")

    lateinit var bean: Bean<T>
        private set

    init {
        if (!canSetEnabled && !default.isEnabled)
            throw IllegalStateException("")

        runBlocking {
            get() // 会初始化 preview
        }
    }

    /**
     * 外界查看值的方式, 默认在未启用时返回空, 以便于生成 genai 配置
     */
    fun value(force: Boolean = false): T? {
        return if (isEnabled() || force || !canSetEnabled)
            bean.value
        else
            null
    }

    fun isEnabled(): Boolean {
        return bean.isEnabled
    }

    suspend fun set(str: String, isEnabled: Boolean = bean.isEnabled): Result {
        val t = parseFromString(str)
            ?: run {
                return Result(false, "parse \"$str\" as T failed")
            }
        val bean = Bean(t, isEnabled)
        return set(bean)
    }

    suspend fun set(block: Builder<T>.() -> Unit): Result {
        val builder = Builder(bean)
        builder.block()
        val bean = Bean(builder.value, builder.isEnabled)
        return set(bean)
    }

    open suspend fun set(bean: Bean<T>): Result {
        if (!canSetEnabled && this.bean.isEnabled != bean.isEnabled) return Result(
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
                    this.bean = bean
            }
    }

    @Suppress("UNCHECKED_CAST")
    protected open suspend fun get(): Bean<T> {
        val json = key.getValue("")
        logE(TAG, json)

        return if (json.isNotEmpty()) {
            val dClazz = getAbsoluteTClass()
            val bean = json.toJsonClass<Bean<T>>()!!
            val vClazz = bean.value!!::class.java
            if (vClazz != dClazz) {
                logE(
                    TAG,
                    "class conflict: ${dClazz.simpleName} <==> ${vClazz.simpleName}"
                )
                val num = bean.value as Number
                val t = parseFromNumber(num, dClazz) as? T
                logE(TAG, "fix value from $num to $t")
                bean.copy(value = t ?: default.value)
            } else
                bean
        } else {
            default
        }.also {
            logE(TAG, "parse from:${json}to:${it}")
            bean = it
            if (json == "")
                set(it)
        }
    }

    protected abstract fun parseFromString(string: String): T?

    protected abstract fun onValidate(bean: Bean<T>): Result

    private fun getAbsoluteTClass(): Class<out T> {
        return default.value!!::class.java
    }


    override fun equals(other: Any?): Boolean {
        if (other is Setting<*>)
            return bean == other.bean
        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + bean.hashCode()
        return result
    }
}