package com.tv.app.model

import com.google.ai.client.generativeai.type.FunctionCallPart
import com.tv.app.model.interfaces.IFuncHandler
import com.tv.app.old.func.FuncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FuncHandler : IFuncHandler<FunctionCallPart> {
    override suspend fun handleParts(functionCalls: List<FunctionCallPart>): Map<String, JSONObject> {
        return handle(functionCalls.map { it.name to it.args })
    }

    override suspend fun handle(
        functionCalls: List<Pair<String, Map<String, String?>?>>
    ): Map<String, JSONObject> {
        val results = mutableMapOf<String, JSONObject>()
        withContext(Dispatchers.IO) {
            functionCalls.forEach { (name, args) ->
                if (args == null)
                    return@forEach

                val jsonStr = FuncManager.executeFunction(name, args)
                results[name] = JSONObject(jsonStr)
            }
        }
        return results
    }
}