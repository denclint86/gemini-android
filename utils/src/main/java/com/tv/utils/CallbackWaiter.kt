package com.tv.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 通用回调等待器, 将回调式操作转换为协程的同步调用
 */
object CallbackWaiter {

    /**
     * 等待回调结果
     * @param timeoutMs 超时时间 (毫秒), 为 null 时不设置超时
     * @param block 配置回调处理逻辑
     * @return 回调结果
     */
    suspend fun <T> await(
        timeoutMs: Long? = null,
        block: CallbackHandler<T>.() -> Unit
    ): T? {
        return if (timeoutMs != null) {
            withTimeoutOrNull(timeoutMs) {
                doAwait(block)
            }
        } else {
            doAwait(block)
        }
    }

    private suspend fun <T> doAwait(block: CallbackHandler<T>.() -> Unit): T =
        suspendCancellableCoroutine { continuation ->
            val handler = object : CallbackHandler<T> {
                override fun registerCallback(trigger: (T) -> Unit) {
                    // 由调用者实现具体的回调注册逻辑
                }

                override fun onResult(result: T) {
                    continuation.safeResume(result)
                }

                override fun onError(error: Throwable) {
                    continuation.safeResumeWithException(error)
                }
            }

            try {
                handler.apply(block)
                handler.registerCallback { result ->
                    handler.onResult(result)
                }
            } catch (e: Exception) {
                continuation.safeResumeWithException(e)
            }

            continuation.invokeOnCancellation {
                // 可添加清理逻辑, 比如取消回调监听
            }
        }

    private fun <T> CancellableContinuation<T>.safeResume(value: T) = runCatching {
        if (this.isCompleted)
            resume(value)
    }

    private fun <T> CancellableContinuation<T>.safeResumeWithException(t: Throwable) = runCatching {
        if (this.isCompleted)
            resumeWithException(t)
    }
}

/**
 * 回调处理接口
 */
interface CallbackHandler<T> {
    /**
     * 注册回调, 调用方需实现具体的触发逻辑
     * @param trigger 触发回调的函数
     */
    fun registerCallback(trigger: (T) -> Unit)

    /**
     * 处理成功结果
     */
    fun onResult(result: T)

    /**
     * 处理错误
     */
    fun onError(error: Throwable)
}

//
///**
// * 扩展函数：为 ActivityResultLauncher 提供便捷的 await 方法
// */
//suspend fun <T> androidx.activity.result.ActivityResultLauncher<T>.await(
//    input: T,
//    timeoutMs: Long? = 30_000
//): androidx.activity.result.ActivityResult? {
//    return CallbackAwaiter.await(timeoutMs) {
//        registerCallback { result ->
//            launch(input)
//            onResult(androidx.activity.result.ActivityResult(RESULT_OK, result))
//        }
//    }
//}
