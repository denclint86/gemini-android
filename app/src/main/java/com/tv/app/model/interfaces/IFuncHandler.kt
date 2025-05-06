package com.tv.app.model.interfaces

import org.json.JSONObject

interface IFuncHandler {
    suspend fun handle(
        functionCalls: List<Pair<String, Map<String, String?>?>>
    ): Map<String, JSONObject>
}