package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.AppType
import com.tv.app.getAllInstalledApps
import com.tv.shizuku.feedback

data object GetAppListModel : BaseFuncModel() {
    override val name: String
        get() = "get_installed_apps"

    override val description: String
        get() = "Query the installed apps on the device, returning their package names and application names."

    override val parameters: List<Schema<*>> = listOf(
        Schema.str(
            "type", "The type of app you want to query. Options: " +
                    "${AppType.ALL.v}, " +
                    "${AppType.USER.v}, " +
                    "${AppType.SYSTEM.v}."
        )
    )

    override val requiredParameters: List<String> = listOf("type")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        try {
            val type = AppType.entries.find { it.v == args.readAsString("type") } ?: AppType.USER

            val appList = getAllInstalledApps(type)

            if (appList.isEmpty()) {
                return defaultMap("error", "error getting app list")
            }

            return mapOf(
                "status" to "success",
                "apps" to appList
            )
        } catch (t: Throwable) {
            return defaultMap("error", t.feedback())
        }
    }
}