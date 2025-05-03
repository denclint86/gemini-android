package com.tv.app.model

import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ResponseHandler : IResponseHandler {
    override fun handle(response: GenerateContentResponse): IResponseHandler.ProcessResult {
        val responseText = response.text ?: ""
        val functionCalls = response.functionCalls.map { it.name to it.args }
        return IResponseHandler.ProcessResult(
            text = responseText,
            functionCalls = functionCalls
        )
    }

    override suspend fun handle(responseFlow: Flow<GenerateContentResponse>): Flow<IResponseHandler.ProcessResult> {
        return responseFlow.map { handle(it) }
    }
}