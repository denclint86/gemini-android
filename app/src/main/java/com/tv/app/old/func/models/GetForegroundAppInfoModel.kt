package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.old.accessibility.MyAccessibilityService
import com.tv.app.old.accessibility.foreground.ForegroundAppManager

data object GetForegroundAppInfoModel : BaseFuncModel() {
    override val name: String =
        "get_foreground_app"
    override val description: String =
        "Get info of the foreground application: package name and app name)."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("default", "Just simply pass in 0.")
    )
    override val requiredParameters: List<String> = listOf("default")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()

        // 使用 ForegroundAppManager 获取前台应用信息
        val appInfo = ForegroundAppManager.getCurrentApp()
        return if (appInfo.first != null) {
            mapOf(
                "status" to "ok",
                "packageName" to appInfo.first,
                "appName" to appInfo.second
            )
        } else {
            defaultMap("error", "foreground app info is null")
        }
    }
}