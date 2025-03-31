package com.tv.app.shell.executors

import com.tv.app.shell.Shell
import com.tv.app.shell.ShellResult
import com.tv.shizuku.ShizukuControl
import com.tv.shizuku.feedback
import com.zephyr.global_values.TAG
import com.zephyr.log.logD
import com.zephyr.log.logE
import com.zephyr.log.toLogString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShizukuExecutor : Shell {

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = withContext(Dispatchers.IO) {
                ShizukuControl.exec("echo test")
            }
            val output = result.output
            output.trim() == "test"
        } catch (e: Exception) {
            logE(TAG, "Shizuku 不可用:\n${e.toLogString()}")
            false
        }
    }

    override suspend fun exec(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val execResult = ShizukuControl.exec(command)
            val exitCode = execResult.exitCode
            val output = execResult.output
            logD(TAG, "Shizuku 执行: $command -> $output") // Added success logging
            return@withContext ShellResult(exitCode, output)
        } catch (e: Exception) {
            logE(TAG, "Shizuku 执行失败: $command -> ${e.toLogString()}") // Added exception logging
            return@withContext ShellResult(null, e.feedback())
        }
    }

    fun release() {
        ShizukuControl.release()
    }
}
