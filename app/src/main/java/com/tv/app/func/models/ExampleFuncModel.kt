package com.tv.app.func.models

import com.google.ai.client.generativeai.type.Schema
import com.tv.app.func.BaseFuncModel


/**
 * 实现示例
 */
object ExampleFuncModel : BaseFuncModel<ExampleFuncModel.JSONResult>() {
    override val name: String = "get_user_email_address"
    override val description: String =
        "通过传入一个密钥 `key` 来获取邮箱地址，返回值是含有调用状态的结果的`json`语句"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("key", "a string given by user")
    )
    override val requiredParameters: List<String> = listOf("key")

    override fun call(args: Map<String, Any?>): JSONResult {
        val arg = args["key"] ?: return JSONResult("error", "incorrect function calling")

        return when (arg) {
            "niki" ->
                JSONResult("ok", "ni@gmail.com")

            "tom" ->
                JSONResult("ok", "tom1998@gmail.com")

            "den" ->
                JSONResult("ok", "d_e_nnn@gmail.com")

            else ->
                JSONResult("error", "incorrect key")
        }
    }

    /**
     * 可以自行定义返回的数据类，或者直接返回字符串之类的，来给父类泛型赋值
     */
    data class JSONResult(val status: String, val result: String)
}