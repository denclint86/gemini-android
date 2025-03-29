package com.tv.app.shell


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

data class ShellResult(
    val exitCode: Int?,
    val output: String?
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "exit_code" to exitCode,
        "output" to output
    )
}

interface Shelly {
    fun isAvailable(): Boolean
    suspend fun exec(command: String): ShellResult
}

// Root 执行器
class RootExecutor : Shelly {
    override fun isAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c echo test")
            process.waitFor() == 0
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

// Shizuku 执行器
class ShizukuExecutor : Shelly {
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

// User 执行器
class UserExecutor : Shelly {
    override fun isAvailable(): Boolean = true // User 模式总是可用

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