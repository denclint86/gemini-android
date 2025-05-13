package com.tv.app.old.func.models

import com.google.ai.client.generativeai.type.Schema
import com.zephyr.extension.thread.runOnMain
import com.zephyr.extension.widget.toast

data object ToastModel : BaseFuncModel() {
    override val name: String = "display_toast"
    override val description: String =
        "向用户发送 toast 消息"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("msg", "toast 的消息内容")
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