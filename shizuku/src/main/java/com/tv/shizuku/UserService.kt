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

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }

            val exitCode = process.waitFor()
            val execResult = ExecResult()
            execResult.exitCode = exitCode
            execResult.output = output.toString()
            return execResult

        } catch (e: SecurityException) {
            throw RuntimeException("Permission denied: ${e.message}", e)
        } catch (e: IOException) {
            throw RuntimeException("IO error during execution: ${e.message}", e)
        } catch (e: InterruptedException) {
            throw RuntimeException("Execution interrupted: ${e.message}", e)
        } finally {
            process?.destroyForcibly() // 更强的进程终止方式
        }
    }
}