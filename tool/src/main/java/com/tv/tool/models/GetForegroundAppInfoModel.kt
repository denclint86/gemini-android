package com.tv.tool.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.utils.accessibility.Accessibility

data object GetForegroundAppInfoModel : BaseFuncModel() {
    override val name: String =
        "get_foreground_app_info"
    override val description: String =
        "获取用户设备当前的前台应用信息"
    override val parameters: List<Schema<*>> = defaultParameters
    override val requiredParameters: List<String> = defaultRequiredParameters

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val accessibility = Accessibility.instance ?: return accessibilityErrorMap()

        val appInfo = accessibility.currentApp ?: return errorMap("前台应用信息为空")

        return mapOf(
            "packageName" to appInfo.first,
            "appName" to appInfo.second
        )
    }
}