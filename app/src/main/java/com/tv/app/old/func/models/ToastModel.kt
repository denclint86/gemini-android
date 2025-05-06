package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema
import com.zephyr.extension.thread.runOnMain
import com.zephyr.extension.widget.toast

data object ToastModel : BaseFuncModel() {
    override val name: String = "send_toast"
    override val description: String =
        "Displays a toast message to the user."
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("msg", "the message to display in the toast.")
    )
    override val requiredParameters: List<String> = listOf("msg")
    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val msg = args.readAsString("msg") ?: return errorFuncCallMap()

        try {
            runOnMain {
                toast(msg)
            }
            return okMap()
        } catch (t: Throwable) {
            return defaultMap("error", t.toSimpleLog())
        }
    }
}