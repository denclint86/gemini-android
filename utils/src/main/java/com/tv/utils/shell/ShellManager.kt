package com.tv.utils.shell

import com.tv.utils.shell.executors.RootExecutor
import com.tv.utils.shell.executors.ShizukuExecutor
import com.tv.utils.shell.executors.UserExecutor
import com.zephyr.global_values.TAG
import com.zephyr.log.logD

class ShellManager {
    val executors = listOf<Shell>(
        RootExecutor(),
        ShizukuExecutor(),
        UserExecutor()
    )

    private var selectedExecutor: Shell? = null
    private val selectedName: String
        get() = selectedExecutor?.javaClass?.simpleName ?: "no_executor"

    private suspend fun selectExecutor() {
        selectedExecutor = executors.firstOrNull { it.isAvailable() }
        logD(TAG, "working as: $selectedName")
    }

    suspend fun exec(command: String): ShellResult {
        if (selectedExecutor == null)
            selectExecutor()
        val executor = selectedExecutor ?: throw Throwable("call select but no executor was chosen")
        if (!executor.isAvailable())
            throw Throwable("$selectedName is unavailable!")
        return executor.exec(command)
    }

    fun release() {
        (selectedExecutor as? ShizukuExecutor)?.release()
    }

    suspend fun isRootAvailable(): Boolean = executors[0].isAvailable()
    suspend fun isShizukuAvailable(): Boolean = executors[1].isAvailable()
}