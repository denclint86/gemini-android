package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.utils.count
import com.zephyr.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data object GetAppListModel : ShellExecutorModel() {
    override val name: String = "get_apps_list"
    override val description: String = "查询设备上已安装应用的包名"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str(
            "type",
            "应用类型. (Options: '${AppType.ALL}', '${AppType.USER}', '${AppType.SYSTEM})"
        )
    )
    override val requiredParameters: List<String> = listOf("type")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        return try {
            val typeString = args.readAsString("type")
            val type = fromString(typeString)

            val appList = getAllInstalledApps(type)
            if (appList.isEmpty())
                defaultMap("failed", "应用信息列表为空")
            else
                defaultMap("success", appList)
        } catch (t: Throwable) {
            defaultMap("error", "shell 命令执行失败")
        }
    }

    private suspend fun getAllInstalledApps(appType: AppType): Set<String> =
        withContext(Dispatchers.IO) {
            val appSet = mutableSetOf<String>()

            // 根据 appType 选择 Shell 命令
            val command = when (appType) {
                AppType.ALL -> "pm list packages"
                AppType.SYSTEM -> "pm list packages -s" // 系统应用
                AppType.USER -> "pm list packages -3"   // 用户安装的应用
            }

            // 执行 Shell 命令
            val result = count("执行 shell") { runShell(command) }
            val output = result.readAsString("output")

            if (output.isNullOrEmpty()) {
                logE("getAllInstalledApps", "Shell 命令失败: $result")
                throw IllegalStateException()
            }

            // 提取包名
            count("提取") {
                output.lines()
                    .filter { it.startsWith("package:") }
                    .map { it.removePrefix("package:") }
                    .toCollection(appSet)
            }

            appSet
        }

    enum class AppType {
        ALL, SYSTEM, USER
    }

    private fun fromString(value: String?): AppType = when (value?.lowercase()) {
        "all" -> AppType.ALL
        "system" -> AppType.SYSTEM
        else -> AppType.USER
    }

//    data class AppInfo(val name: String, val packageName: String)
}