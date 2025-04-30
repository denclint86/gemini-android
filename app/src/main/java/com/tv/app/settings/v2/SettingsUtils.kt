package com.tv.app.settings.v2

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

object SettingsUtils {
    val gson: Gson by lazy { Gson() }
    private val prettyGson: Gson by lazy { GsonBuilder().setPrettyPrinting().create() }

    inline fun <reified T> String.toBean(): SettingBean<T> {
        val type = object : TypeToken<SettingBean<T>>() {}.type
        return gson.fromJson(this, type)
    }

    fun Any?.toPrettyJson(): String {
        fun CharSequence.toJsonElement(): JsonElement? = try {
            JsonParser.parseString(this.toString())
        } catch (_: Throwable) {
            null
        }

        val result = if (this is CharSequence) {
            prettyGson.toJson(this.toJsonElement())
        } else
            prettyGson.toJson(this)
        return if (result == "null") "" else result
    }

    fun Any?.toJson(): String {
        val result = gson.toJson(this)
        return if (result == "null") "" else result
    }
}

