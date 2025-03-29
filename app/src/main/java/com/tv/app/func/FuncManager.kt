package com.tv.app.func

import com.google.gson.annotations.SerializedName
import com.tv.app.func.models.BaseFuncModel
import com.zephyr.global_values.TAG
import com.zephyr.log.logE
import com.zephyr.net.toJson
import com.zephyr.net.toPrettyJson
import org.json.JSONObject
import kotlin.reflect.KClass

/**
 * 所有的函数 model 都要在这里添加
 */
object FuncManager {
    private val _functionMap = mutableMapOf<String, BaseFuncModel>()
    val functionMap: Map<String, BaseFuncModel>
        get() = _functionMap

    init {
        // 注册所有实现
        val list = getSealedClassObjects(BaseFuncModel::class)
        list.forEach { model ->
            _functionMap[model.name] = model
        }
    }

    fun getDeclarations() = _functionMap.values.map { it.getFuncDeclaration() }

    /**
     * 统一函数调用入口
     *
     * 保证输出一个 json 对象
     */
    suspend fun executeFunction(functionName: String, args: Map<String, String?>): JSONObject {
        val result =
            _functionMap[functionName]?.call(args) ?: Error(functionName)

        logE(
            TAG, "$functionName 执行结果:\n" + when (result) {
                is Map<*, *> -> result.toPrettyJson()
                is Error -> result.toPrettyJson()
                else -> result.toString()
            }
        )

        // 转换为JSONObject并返回
        return when (result) {
            is Map<*, *> -> JSONObject(result.toJson())
            is Error -> JSONObject(result.toJson())
            else -> JSONObject()  // 防御性编程，处理意外情况
        }
    }

    private data class Error(
        @SerializedName("unknown_function")
        val msg: String
    )
}

/**
 * 反射筛选一个封装类的 object 实现
 */
fun <T : Any> getSealedClassObjects(sealedClass: KClass<T>): List<T> {
    require(sealedClass.isSealed) { "传入的参数必须是封装类" }

    return sealedClass.sealedSubclasses
        .mapNotNull { subclass ->
            subclass.objectInstance
        }
}