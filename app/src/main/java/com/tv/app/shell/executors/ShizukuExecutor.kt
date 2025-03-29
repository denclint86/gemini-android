package com.tv.app.shell.executors

import com.tv.app.shell.Shell
import com.tv.app.shell.ShellResult
import com.tv.shizuku.ShizukuManager
import com.tv.shizuku.feedback
import com.tv.shizuku.feedbackStr
import com.zephyr.global_values.TAG
import com.zephyr.log.logD
import com.zephyr.log.logE
import com.zephyr.log.toLogString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ShizukuExecutor : Shell {
    init {
        ShizukuManager.init()
    }

    override fun isAvailable(): Boolean =
        ShizukuManager.isRunning() && ShizukuManager.hasPermission()

    override suspend fun exec(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            if (!isAvailable()) {
                val feedback = "Shizuku unavailable".feedbackStr()
                logE(TAG, "Shizuku 执行失败: $command -> $feedback")
                return@withContext ShellResult(null, feedback)
            }

            if (!ShizukuManager.isConnected()) {
                ShizukuManager.bindService()
                while (!ShizukuManager.isConnected()) {
                    delay(100)
                }
            }

            val r = ShizukuManager.exec(command)
            logD(TAG, "Shizuku 执行: $command -> ${r.output}")
            ShellResult(r.exitCode, r.output)
        } catch (t: Throwable) {
            logE(TAG, "Shizuku 执行失败: $command -> ${t.toLogString()}")
            ShellResult(null, t.feedback())
        }
    }

    fun release() {
        ShizukuManager.release()
    }
}
