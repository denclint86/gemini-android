package com.tv.app.settings.v2

import com.google.gson.annotations.SerializedName
import com.tv.app.settings.v2.SettingsUtils.toBean
import com.tv.app.settings.v2.SettingsUtils.toJson
import com.tv.app.settings.v2.SettingsUtils.toPrettyJson


data class SettingBean<out T>(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: T,
    @SerializedName("enabled") val enabled: Boolean
) {
    companion object {
        inline fun <reified T> fromJson(json: String): SettingBean<T> = json.toBean<T>()
    }

    fun <T> SettingBean<T>.toJson(pretty: Boolean = false): String {
        return if (pretty)
            SettingsUtils.toPrettyJson()
        else
            SettingsUtils.toJson()
    }
}