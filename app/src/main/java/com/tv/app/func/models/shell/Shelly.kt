package com.tv.app.func.models.shell


import com.tv.app.ShizukuHelper
import com.zephyr.global_values.TAG
import com.zephyr.log.logD
import com.zephyr.log.logE
import com.zephyr.log.toLogString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface Shelly {
    fun isAvailable(): Boolean
    suspend fun exec(command: String): String?
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

    override suspend fun exec(command: String): String? = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su -c $command")
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            logD(TAG, "Root 执行: $command -> $output")
            output.trim()
        } catch (e: Exception) {
            logE(TAG, "Root 执行失败: $command\n${e.toLogString()}")
            null
        }
    }
}

// Shizuku 执行器
class ShizukuExecutor : Shelly {
    private val shizukuHelper = ShizukuHelper()

    init {
        shizukuHelper.init()
    }

    override fun isAvailable(): Boolean {
        return shizukuHelper.isShizukuAvailable()
    }

    override suspend fun exec(command: String): String? {
        return shizukuHelper.execCommand(command)
    }

    fun release() {
        shizukuHelper.release()
    }
}

// User 执行器
class UserExecutor : Shelly {
    override fun isAvailable(): Boolean = true // User 模式总是可用

    override suspend fun exec(command: String): String? = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            logD(TAG, "User 执行: $command -> $output")
            output.trim()
        } catch (e: Exception) {
            logE(TAG, "User 执行失败: $command\n${e.toLogString()}")
            null
        }
    }
}