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

class UserExecutor : Shell {
    override fun isAvailable(): Boolean = true

    override suspend fun exec(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val code = process.waitFor()
            logD(TAG, "User 执行: $command -> $output")
            ShellResult(code, output.trim())
        } catch (e: Exception) {
            logE(TAG, "User 执行失败: $command -> ${e.toLogString()}")
            ShellResult(null, e.feedback())
        }
    }
}