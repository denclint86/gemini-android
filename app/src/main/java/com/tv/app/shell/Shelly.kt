package com.tv.app.shell


import com.tv.shizuku.ShizukuManager
import com.tv.shizuku.feedback
import com.tv.shizuku.feedbackStr
import com.zephyr.global_values.TAG
import com.zephyr.log.logD
import com.zephyr.log.logE
import com.zephyr.log.toLogString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
            logE(TAG, "Root 执行失败: $command\n${e.toLogString()}")
            ShellResult(null, e.feedback())
        }
    }
}

// Shizuku 执行器
class ShizukuExecutor : Shelly {
    init {
        ShizukuManager.init()
    }

    override fun isAvailable(): Boolean {
        val isRunning = ShizukuManager.isShizukuRunning()
        val hasPermission = ShizukuManager.hasPermission()
        return isRunning && hasPermission
    }

    override suspend fun exec(command: String): ShellResult = suspendCoroutine { continuation ->
        if (!isAvailable()) {
            continuation.resume(ShellResult(null, "Shizuku is unavailable".feedbackStr()))
            return@suspendCoroutine
        }

        if (!ShizukuManager.isConnected()) {
            ShizukuManager.bindService()
        }

        try {
            val connectionListener = object : ShizukuManager.ConnectionListener {
                override fun onServiceConnected() {
                    ShizukuManager.removeConnectionListener(this)
                    val pair = ShizukuManager.exec(command)
                    continuation.resume(
                        ShellResult(
                            pair.first,
                            pair.second ?: "execution failed".feedbackStr()
                        )
                    )
                }

                override fun onServiceDisconnected() {
                    ShizukuManager.removeConnectionListener(this)
                    continuation.resume(
                        ShellResult(
                            null,
                            "Shizuku service disconnected".feedbackStr()
                        )
                    )
                }
            }

            ShizukuManager.addConnectionListener(connectionListener)

            if (ShizukuManager.isConnected()) {
                val pair = ShizukuManager.exec(command)
                ShizukuManager.removeConnectionListener(connectionListener)
                continuation.resume(
                    ShellResult(
                        pair.first,
                        pair.second ?: "execution failed".feedbackStr()
                    )
                )
            }
        } catch (e: Exception) {
            val r = ShellResult(null, e.feedback())
            continuation.resume(r)
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
            logE(TAG, "User 执行失败: $command\n${e.toLogString()}")
            ShellResult(null, e.feedback())
        }
    }
}