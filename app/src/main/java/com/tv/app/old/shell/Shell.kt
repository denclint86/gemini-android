package com.tv.app.old.shell

interface Shell {
    suspend fun isAvailable(): Boolean
    suspend fun exec(command: String): ShellResult
}
