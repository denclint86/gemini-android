package com.tv.app.func

import com.google.ai.client.generativeai.type.Schema


/**
 * 实现示例
 */
class ExampleFuncModel(
    override val name: String = FUNC_NAME,
    override val description: String = "通过传入一个密钥 'key' 来获取邮箱地址",
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("key", "a string given by user"),
    ),
    override val requiredParameters: List<String> = listOf("key")
) : BaseFuncModel<ExampleFuncModel.JSONResult>() {
    companion object {
        const val FUNC_NAME = "get_user_email_address"
    }

    override fun call(args: Map<String, Any?>): JSONResult {
        val arg = args["key"] ?: return JSONResult("error", "incorrect function calling")

        return if (arg == "niki") JSONResult("ok", "asd@gmail.com") else JSONResult(
            "error",
            "incorrect key"
        )
    }

    /**
     * 可以自行定义返回的数据类，或者直接返回字符串之类的，来给父类泛型赋值
     */
    data class JSONResult(val status: String, val result: String)
}