package com.tv.app.shell

import com.zephyr.global_values.TAG
import com.zephyr.log.logD

class ShellManager {
    val executors = listOf<Shelly>(
        RootExecutor(),
        ShizukuExecutor(),
        UserExecutor()
    )

    private var selectedExecutor: Shelly? = null
    private val selectedName: String
        get() = selectedExecutor?.javaClass?.simpleName ?: "no_executor"

    init {
        selectExecutor()
    }

    private fun selectExecutor() {
        selectedExecutor = executors.firstOrNull { it.isAvailable() }
        logD(TAG, "working as: $selectedName")
    }

    suspend fun exec(command: String): ShellResult {
        val executor = selectedExecutor ?: throw Throwable("tf")
        if (!executor.isAvailable())
            throw Throwable("$selectedName is unavailable!")
        return executor.exec(command)
    }

    fun release() {
        (selectedExecutor as? ShizukuExecutor)?.release()
    }

    fun isRootAvailable(): Boolean = executors[0].isAvailable()
    fun isShizukuAvailable(): Boolean = executors[1].isAvailable()
}