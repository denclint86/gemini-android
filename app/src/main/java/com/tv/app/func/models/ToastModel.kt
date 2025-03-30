package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.zephyr.extension.thread.runOnMain
import com.zephyr.extension.widget.toast
import com.zephyr.log.toLogString

data object ToastModel : BaseFuncModel() {
    override val name: String = "send_toast"
    override val description: String =
        "Displays a toast message to the user."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("msg", "the message to display in the toast."),
        Schema.int("duration", "optional duration in seconds (default: 2).")
    )
    override val requiredParameters: List<String> = listOf("msg")
    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val msg = args.readAsString("msg") ?: return errorFuncCallMap()
        val duration = args.readAsString("duration")?.toIntOrNull() ?: 2

        try {
            runOnMain {
                toast(msg, true, duration)
            }
            return okMap()
        } catch (t: Throwable) {
            return defaultMap("error", t.toLogString())
        }
    }
}