package com.tv.shizuku

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess

/**
 * Shizuku UserService 实现类
 * 运行在 Shizuku 进程中，具有 Shizuku 授予的系统权限
 */
class UserService : IUserService.Stub() {

    override fun destroy() =
        exitProcess(0)

    override fun exit() =
        destroy()

    override fun exec(command: String): String {
        val result = StringBuilder()
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                result.append(line).append("\n")
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return result.toString()
    }
} 