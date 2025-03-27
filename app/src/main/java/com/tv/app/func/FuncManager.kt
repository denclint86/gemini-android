package com.tv.app.func

import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import com.zephyr.net.toPrettyJson

/**
 * 所有的函数 model 都要在这里添加
 */
object FuncManager {
    // map: <model, func model>
    val functionMap = mapOf<String, BaseFuncModel<*>>(
        ExampleFuncModel.FUNC_NAME to ExampleFuncModel()
    )

    /**
     * 统一函数调用入口
     *
     * 保证输出一个 json 字串（虽然没有硬性要求）
     */
    fun executeFunction(functionName: String, args: Map<String, String?>): String {
        val function = functionMap[functionName]?.getFuncInstance()

        val result = function?.invoke(args) ?: "{\"unknown_function\": \"$functionName\"}"
        val json = result.toJson()
        logE(TAG, "$functionName 执行结果:\n" + result.toPrettyJson()) // 调试用
//        logE(TAG, "$functionName 执行结果:\n$json")
        return json
    }
}