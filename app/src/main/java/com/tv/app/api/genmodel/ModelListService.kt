package com.tv.app.api.genmodel

import com.tv.app.api.genmodel.beans.ListModelsResponse
import com.zephyr.net.ServiceBuilder
import com.zephyr.net.bean.NetResult
import com.zephyr.net.requestEnqueue

class ModelListService {
    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    }

    private val service by lazy {
        ServiceBuilder.create<GenModelListService>(BASE_URL)
    }

    fun listModels(
        key: String,
        apiVersion: String,
        callback: (NetResult<ListModelsResponse>) -> Unit
    ) = requestEnqueue(service.listModels(apiVersion, key), callback)
}