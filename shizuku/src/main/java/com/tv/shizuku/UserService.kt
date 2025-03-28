package com.tv.shizuku

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess

class UserService : IUserService.Stub() {

    override fun destroy() = exitProcess(0)

    override fun exit() = destroy()

    override fun exec(command: String): ExecResult {
        val output = StringBuilder()
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(command)

            // 读取标准输出
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }

            // 获取退出码
            val exitCode = process.waitFor()

            val execResult = ExecResult()
            execResult.exitCode = exitCode
            execResult.output = output.toString()
            return execResult
        } catch (e: IOException) {
            throw RuntimeException("Exec failed: ${e.message}", e)
        } catch (e: InterruptedException) {
            throw RuntimeException("Exec interrupted: ${e.message}", e)
        } finally {
            process?.destroy()
        }
    }
}