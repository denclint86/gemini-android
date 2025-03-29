package com.tv.app.shell


data class ShellResult(
    val exitCode: Int?,
    val output: String?
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "exit_code" to exitCode,
        "output" to output
    )
}
