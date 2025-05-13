package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema

data object LaunchAppModel : ShellExecutorModel() {
    override val name: String = "launch_app"
    override val description: String =
        "通过包名打开用户设备上的应用"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("package_name", "目标 app 的包名")
    )
    override val requiredParameters: List<String> = listOf("package_name")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val packageName = args.readAsString("package_name")
            ?: return errorFuncCallMap()

        return runShell(
            "am start -n \$(pm resolve-activity --brief $packageName | tail -n 1)"
        )
    }
}