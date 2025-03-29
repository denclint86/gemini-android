package com.tv.app.shell

interface Shell {
    fun isAvailable(): Boolean
    suspend fun exec(command: String): ShellResult
}
