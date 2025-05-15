package com.tv.tool.models

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.defineFunction
import com.tv.utils.shell.ShellManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

fun defaultMap(status: String, result: Any? = "") =
    mapOf("status" to status, "result" to result)

fun accessibilityErrorMap() = errorMap("无障碍服务不可用")

fun successMap() = mapOf("status" to "succeed")

fun errorMap(t: Throwable) = errorMap(t.toSimpleLog())

fun errorMap(msg: String) = defaultMap("error", msg)

fun errorFuncCallMap() = errorMap("非法的函数调用")

fun Throwable.toSimpleLog(): String {
    return "message: ${message}\ncause: $cause"
}

suspend fun <T> Map<T, Any?>.readAsString(key: T): String? =
    withContext(Dispatchers.IO) { get(key) as? String }

/**
 * 用来调用命令
 */
class ShellExecutorModelImpl : ShellExecutorModel() {
    override val name: String = ""
    override val description: String = ""
    override val parameters: List<Schema<*>> = listOf()
    override val requiredParameters: List<String> = listOf()

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        return mapOf()
    }
}

sealed class ShellExecutorModel : BaseFuncModel() {
//    override val name: String = "run_shell_command"
//    override val description: String =
//        "Executes a Android shell command on the user's device. return a json with exit code and output. Note: 1. Never run dangerous commands. 2. For commands requiring root privileges, prefer alternative functions when available."
//    override val parameters: List<Schema<*>> = listOf(
//        Schema.str("command", "the shell command to execute"),
//        Schema.str(
//            "timeout",
//            "timeout in milliseconds(optional), if not provided runs without timeout."
//        )
//    )
//    override val requiredParameters: List<String> = listOf("command")

    private val shellManager: ShellManager = ShellManager()

    suspend fun runShell(command: String, timeout: Long? = 15_000): Map<String, Any?> {
        return try {
            // 使用 ShellManager 执行命令, 并根据超时参数处理
            val result = if (timeout != null && timeout > 0) {
                withTimeout(timeout) {
                    shellManager.exec(command)
                }
            } else {
                shellManager.exec(command)
            }

            result.toMap()
        } catch (e: Exception) {
            errorMap(e)
        }
    }
}

/**
 * 函数基类
 */
sealed class BaseFuncModel {
    abstract val name: String // 函数名
    abstract val description: String // 函数的功能描述
    abstract val parameters: List<Schema<*>> // 各个变量的定义
    abstract val requiredParameters: List<String> // 要求的输入参数

    /**
     * 用于调用的函数本体, 为了方便转 json, 直接返回 map
     */
    abstract suspend fun call(args: Map<String, Any?>): Map<String, Any?>

    fun getFuncDeclaration(): FunctionDeclaration = defineFunction(
        name = name,
        description = description,
        parameters = parameters,
        requiredParameters = requiredParameters
    )

    fun getFuncInstance() = ::call

    protected val defaultParameters
        get() = listOf(Schema.str("default", "传入 0 即可"))

    protected val defaultRequiredParameters
        get() = listOf("default")
}