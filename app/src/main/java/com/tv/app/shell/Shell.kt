package com.tv.app.shell

interface Shell {
    suspend fun isAvailable(): Boolean
    suspend fun exec(command: String): ShellResult
}
