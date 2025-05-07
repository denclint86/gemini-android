package com.tv.app.model.interfaces

import org.json.JSONObject

interface IFuncHandler<T> {
    suspend fun handleParts(
        functionCalls: List<T>
    ): Map<String, JSONObject>

    suspend fun handle(
        functionCalls: List<Pair<String, Map<String, String?>?>>
    ): Map<String, JSONObject>
}