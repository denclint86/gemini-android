package com.tv.app.model

import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.tv.app.model.interfaces.IResponseHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ResponseHandler : IResponseHandler<GenerateContentResponse> {
    override fun handle(response: GenerateContentResponse): IResponseHandler.ProcessResult<GenerateContentResponse> {
        val responseText = response.text ?: ""
        val functionCalls = response.functionCalls.map { it.name to it.args }
        return IResponseHandler.ProcessResult<GenerateContentResponse>(
            text = responseText,
            functionCalls = functionCalls,
            raw = response
        )
    }

    override suspend fun handle(responseFlow: Flow<GenerateContentResponse>): Flow<IResponseHandler.ProcessResult<GenerateContentResponse>> {
        return responseFlow.map { handle(it) }
    }
}