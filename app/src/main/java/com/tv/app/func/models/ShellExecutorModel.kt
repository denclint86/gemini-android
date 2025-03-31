package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.shell.ShellManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

data object ShellExecutorModel : BaseFuncModel() {
    override val name: String = "run_shell_command"
    override val description: String =
        "Executes a Android shell command on the user's device. return a json with exit code and output. Note: 1. Never run dangerous commands. 2. For commands requiring root privileges, prefer alternative functions when available."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("command", "the shell command to execute"),
        Schema.str(
            "timeout",
            "timeout in milliseconds(optional), if not provided runs without timeout."
        )
    )
    override val requiredParameters: List<String> = listOf("command")

    val shellManager: ShellManager = ShellManager()

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> =
        withContext(Dispatchers.IO) {
            val command = args.readAsString("command")
                ?: return@withContext errorFuncCallMap()
            val timeout = args.readAsString("timeout")?.toLongOrNull()

            try {
                // 使用 ShellManager 执行命令，并根据超时参数处理
                val result = if (timeout != null && timeout > 0) {
                    withTimeout(timeout) {
                        shellManager.exec(command)
                    }
                } else {
                    shellManager.exec(command)
                }

                result.toMap()
            } catch (e: Exception) {
                defaultMap("error", e.toSimpleLog())
            }
        }
}