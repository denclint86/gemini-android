package com.tv.tool.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.utils.accessibility.Accessibility


data object ScreenContentModel : BaseFuncModel() {
    override val name: String = "get_screen_content"
    override val description: String =
        "获取屏幕的视图树信息, 这对分析屏幕非常有用"

    //        "获取屏幕的视图树信息, 这对分析屏幕非常有用. 注意: 调用完这个函数并得到结果后应该停止任务、停止函数调用并等待用户发送截图后再继续任务! 注意, 这条规则很重要!!"
    override val parameters: List<Schema<*>> = defaultParameters
    override val requiredParameters: List<String> = defaultRequiredParameters

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val iAccessibility = Accessibility.instance
            ?: return accessibilityErrorMap()
        return iAccessibility.viewMap ?: accessibilityErrorMap()
    }
}