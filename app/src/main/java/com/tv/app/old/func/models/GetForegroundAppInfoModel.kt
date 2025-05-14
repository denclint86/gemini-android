package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.old.accessibility.MyAccessibilityService
import com.tv.app.old.accessibility.foreground.ForegroundAppManager

data object GetForegroundAppInfoModel : BaseFuncModel() {
    override val name: String =
        "get_foreground_app_info"
    override val description: String =
        "获取用户设备当前的前台应用信息"
    override val parameters: List<Schema<*>> = defaultParameters
    override val requiredParameters: List<String> = defaultRequiredParameters

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        MyAccessibilityService.instance.get() ?: return accessibilityErrorMap()

        val appInfo = ForegroundAppManager.getCurrentApp()
        return if (appInfo.first != null) {
            mapOf(
                "packageName" to appInfo.first,
                "appName" to appInfo.second
            )
        } else {
            errorMap("foreground app info is null")
        }
    }
}