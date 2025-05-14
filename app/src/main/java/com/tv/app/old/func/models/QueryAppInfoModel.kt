package com.tv.app.old.func.models

import android.content.pm.PackageManager
import com.google.ai.client.generativeai.type.Schema
import com.zephyr.global_values.globalContext

data object QueryAppInfoModel : BaseFuncModel() {
    override val name: String = "query_app_info"
    override val description: String = "根据应用包名查询指定应用的信息"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("package_name", "目标应用的包名")
    )
    override val requiredParameters: List<String> = listOf("package_name")

    private val pm: PackageManager
        get() = globalContext!!.packageManager

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val packageName = args.readAsString("package_name")
            ?: return errorFuncCallMap()

        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val packageInfo = pm.getPackageInfo(packageName, 0)

            mapOf(
                "app_name" to pm.getApplicationLabel(appInfo).toString(),
                "version_name" to packageInfo.versionName
            )
        } catch (t: Throwable) {
            errorMap(t)
        }
    }
}