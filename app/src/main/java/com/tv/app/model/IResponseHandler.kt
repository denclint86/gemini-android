package com.tv.app.model

import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.flow.Flow

interface IResponseHandler {
    fun handle(response: GenerateContentResponse): ProcessResult
    suspend fun handle(responseFlow: Flow<GenerateContentResponse>): Flow<ProcessResult>

    data class ProcessResult(
        val text: String,
        val functionCalls: List<Pair<String, Map<String, String?>?>>
    )
}