package com.tv.app.old.func

import com.google.gson.annotations.SerializedName
import com.tv.app.old.func.models.BaseFuncModel
import com.tv.app.utils.getSealedClassObjects
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson

/**
 * 所有的函数 model 都要在这里添加
 */
object FuncManager {
    private val _functionMap = mutableMapOf<String, BaseFuncModel>()
    val functionMap: Map<String, BaseFuncModel>
        get() = _functionMap

    init {
        // 注册所有实现
        val list = getSealedClassObjects(BaseFuncModel::class) { kClass ->
            kClass.objectInstance
        }
        list.forEach { model ->
            _functionMap[model.name] = model
        }
        logE(TAG, "已注册函数: ${functionMap.keys}")
    }

    fun getDeclarations() = _functionMap.values.map { it.getFuncDeclaration() }

    /**
     * 统一函数调用入口
     *
     * 保证输出一个 json 字串
     */
    suspend fun executeFunction(functionName: String, args: Map<String, String?>): String {
        val result =
            _functionMap[functionName]?.call(args) ?: Error(functionName)
        return result.toJson()
    }

    private data class Error(
        @SerializedName("unknown_function")
        val msg: String
    )
}