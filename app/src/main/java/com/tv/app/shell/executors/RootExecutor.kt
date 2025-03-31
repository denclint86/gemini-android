package com.tv.app.shell.executors

import com.tv.app.shell.Shell
import com.tv.app.shell.ShellResult
import com.tv.shizuku.feedback
import com.zephyr.global_values.TAG
import com.zephyr.log.logD
import com.zephyr.log.logE
import com.zephyr.log.toLogString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class RootExecutor : Shell {
    companion object {
        const val TEST_TIMEOUT = 5000L
    }

    override suspend fun isAvailable(): Boolean {
        return try {
            val process = withContext(Dispatchers.IO) {
                Runtime.getRuntime().exec("su -c echo test")
            }
            val output = withContext(Dispatchers.IO) {
                process.inputStream.bufferedReader().use { it.readText() }
            }
            val exitCode = withContext(Dispatchers.IO) {
                process.waitFor(TEST_TIMEOUT, TimeUnit.MILLISECONDS) // Add timeout
            }
            exitCode && process.exitValue() == 0 && output.trim() == "test"
        } catch (e: Exception) {
            logE(TAG, "Root 不可用:\n${e.toLogString()}")
            false
        }
    }

    override suspend fun exec(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su -c $command")
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val code = process.waitFor()
            logD(TAG, "Root 执行: $command -> $output")
            ShellResult(code, output.trim())
        } catch (e: Exception) {
            logE(TAG, "Root 执行失败: $command -> ${e.toLogString()}")
            ShellResult(null, e.feedback())
        }
    }
}
