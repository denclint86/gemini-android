package com.tv.app.func

import com.tv.app.func.models.ExampleFuncModel
import com.tv.app.func.models.ExampleFuncModel2
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import com.zephyr.net.toPrettyJson

/**
 * 所有的函数 model 都要在这里添加
 */
object FuncManager {
    val functionMap = mapOf<String, BaseFuncModel<*>>(
        ExampleFuncModel.name to ExampleFuncModel,
        ExampleFuncModel2.name to ExampleFuncModel2
    )

    fun getDeclarations() = functionMap.values.map { it.getFuncDeclaration() }

    /**
     * 统一函数调用入口
     *
     * 保证输出一个 json 字串（虽然没有硬性要求）
     */
    fun executeFunction(functionName: String, args: Map<String, Any?>): String {
        val result =
            functionMap[functionName]?.call(args) ?: "{\"unknown_function\": \"$functionName\"}"

        val json = result.toJson()

        logE(TAG, "$functionName 执行结果:\n" + result.toPrettyJson()) // 调试用
//        logE(TAG, "$functionName 执行结果:\n$json")
        return json
    }
}