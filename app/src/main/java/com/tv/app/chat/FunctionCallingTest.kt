package com.tv.app.chat

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson

sealed class FunctionResult {
    class Success<T>(val state: String, val result: T) : FunctionResult()
    class Error(val state: String, val msg: String) : FunctionResult()
}

// 函数实现类
class FunctionProvider {
    private fun test(args: Map<String, String?>): String {
        val arg = args[EXAMPLE_TOOL_ARG_NAME] ?: return "incorrect function calling"

        return if (arg == "niki") "asd@gmail.com" else "incorrect key"
    }

    private val functionMap = mapOf<String, (Map<String, String?>) -> String>(
        EXAMPLE_TOOL_FUNC_NAME to ::test
    )

    fun executeFunction(functionName: String, args: Map<String, String?>): FunctionResult {
        val function = functionMap[functionName]
        val raw = function?.invoke(args)
        val result = if (raw == null) {
            FunctionResult.Error("error", "unknown function - $functionName")
        } else {
            FunctionResult.Success("ok", function.invoke(args))
        }
        logE(TAG, result.toJson())
        return result
    }
}

const val EXAMPLE_TOOL_FUNC_NAME = "get_user_email_address"
const val EXAMPLE_TOOL_ARG_NAME = "key"


// 直接实现 Tool
val exampleTool = Tool(
    functionDeclarations = listOf(
        defineFunction(
            name = EXAMPLE_TOOL_FUNC_NAME,
            description = "enter user's given string, returns an email address if correct",
            parameters = listOf(
                Schema.str(EXAMPLE_TOOL_ARG_NAME, "a string given by user"),
//                Schema.int("b", "The number of times to repeat the string")
            ),
            requiredParameters = listOf(EXAMPLE_TOOL_ARG_NAME)
        )
    )
)
