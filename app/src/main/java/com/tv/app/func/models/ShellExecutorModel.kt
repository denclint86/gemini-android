package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.func.models.shell.ShellManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

data object ShellExecutorModel : BaseFuncModel() {
    override val name: String = "run_android_shell"
    override val description: String =
        "Executes a shell command on a rooted Android device with superuser privileges if available, otherwise runs as normal user"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("command", "the shell command to execute"),
        Schema.str(
            "timeout",
            "optional timeout in milliseconds, if not provided runs without timeout"
        )
    )
    override val requiredParameters: List<String> = listOf("command")

    private val shellManager: ShellManager by lazy { ShellManager() }

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> =
        withContext(Dispatchers.IO) {
            val command = args["command"] as? String
                ?: return@withContext defaultMap("error", "command parameter is required")
            val timeout = args["timeout"]?.toString()?.toLongOrNull()

            try {
                // 使用 ShellManager 执行命令，并根据超时参数处理
                val result = if (timeout != null && timeout > 0) {
                    withTimeout(timeout) {
                        shellManager.exec(command)
                    }
                } else {
                    shellManager.exec(command)
                }

                // 处理执行结果
                when {
                    result.isNotEmpty() -> defaultMap("success", result)
                    else -> defaultMap("success", "Command executed successfully")
                }
            } catch (e: SecurityException) {
                defaultMap("error", "Permission denied: ${e.toSimpleLog()}")
            } catch (e: Exception) {
                defaultMap("error", "Command execution failed: ${e.toSimpleLog()}")
            }
        }

    // 异常日志简化
    private fun Throwable.toSimpleLog(): String {
        return "message: ${message}\ncause: $cause"
    }
}