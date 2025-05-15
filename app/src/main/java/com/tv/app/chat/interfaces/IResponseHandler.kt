package com.tv.app.chat.interfaces

import kotlinx.coroutines.flow.Flow

interface IResponseHandler<T> {
    fun handle(response: T): ProcessResult<T>
    suspend fun handle(responseFlow: Flow<T>): Flow<ProcessResult<T>>

    data class ProcessResult<T>(
        val text: String,
        val functionCalls: List<Pair<String, Map<String, String?>?>>,
        val raw: T
    )
}