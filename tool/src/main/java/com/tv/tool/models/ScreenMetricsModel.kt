package com.tv.tool.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.utils.getScreenSize

data object ScreenMetricsInfoModel : BaseFuncModel() {
    override val name: String = "get_screen_metrics_info"
    override val description: String =
        "获取用户设备的屏幕长宽像素大小, 用于为基于坐标的操作提供数值计算参考"
    override val parameters: List<Schema<*>> = defaultParameters
    override val requiredParameters: List<String> = defaultRequiredParameters

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        return try {
            val pair = getScreenSize()
            val w = pair.first
            val h = pair.second

            mapOf(
                "width" to w,
                "height" to h
            )
        } catch (t: Throwable) {
            errorMap(t)
        }
    }
}