package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

data object RunShellModel : BaseFuncModel() {
    override val name: String = "run_android_shell"
    override val description: String =
        "Executes a shell command on a rooted Android device with superuser privileges if available, otherwise runs as normal user"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("command", "the shell command to execute"),
        Schema.str("timeout", "optional timeout in seconds, if not provided runs without timeout")
    )
    override val requiredParameters: List<String> = listOf("command")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> =
        withContext(Dispatchers.IO) {
            val command = args["command"] as? String
                ?: return@withContext defaultMap("error", "command parameter is required")
            val timeout = args["timeout"]?.toString()?.toLongOrNull()

            var process: Process? = null
            var outputStream: DataOutputStream? = null
            try {
                // 如果设备已 root，则使用 su 执行命令，否则直接执行
                val isRooted = isRootAvailable()
                process = if (isRooted) {
                    Runtime.getRuntime().exec("su")
                } else {
                    Runtime.getRuntime().exec(command)
                }

                val output = StringBuilder()
                val errorOutput = StringBuilder()

                if (isRooted) {
                    // 通过 su shell 执行命令
                    outputStream = DataOutputStream(process.outputStream)
                    outputStream.writeBytes("$command\n")
                    outputStream.writeBytes("exit\n")
                    outputStream.flush()
                }

                // 读取标准输出
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.append(line).append("\n")
                    }
                }

                // 读取错误输出
                BufferedReader(InputStreamReader(process.errorStream)).use { errorReader ->
                    var line: String?
                    while (errorReader.readLine().also { line = it } != null) {
                        errorOutput.append(line).append("\n")
                    }
                }

                // 处理超时
                val exitCode = if (timeout != null && timeout > 0) {
                    val timeoutMillis = timeout * 1000
                    val startTime = System.currentTimeMillis()
                    while (process.isAlive && (System.currentTimeMillis() - startTime) < timeoutMillis) {
                        Thread.sleep(100)
                    }
                    if (process.isAlive) {
                        process.destroy()
                        return@withContext defaultMap(
                            "error",
                            "Command timed out after ${timeout}s"
                        )
                    }
                    process.exitValue()
                } else {
                    process.waitFor()
                }

                val result = output.toString().trim()
                val error = errorOutput.toString().trim()
                when {
                    exitCode == 0 && result.isNotEmpty() -> defaultMap("success", result)
                    exitCode == 0 -> defaultMap("success", "Command executed successfully")
                    error.isNotEmpty() -> defaultMap("error", error)
                    else -> defaultMap("error", "Command failed with exit code $exitCode")
                }
            } catch (e: SecurityException) {
                defaultMap("error", "Root access denied: ${e.toSimpleLog()}")
            } catch (e: Exception) {
                defaultMap("error", "Command execution failed: ${e.toSimpleLog()}")
            } finally {
                outputStream?.close()
                process?.destroy()
            }
        }

    // 检查设备是否已 root 并可用
    private suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            val output = DataOutputStream(process.outputStream)
            val input = BufferedReader(InputStreamReader(process.inputStream))
            output.writeBytes("whoami\n")
            output.writeBytes("exit\n")
            output.flush()

            val result = input.readLine()
            process.waitFor()
            result == "root"
        } catch (e: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun Throwable.toSimpleLog(): String {
        return "message: ${message}\ncause: $cause"
    }
}