package com.tv.app.call

import com.google.gson.annotations.SerializedName
import com.zephyr.net.ServiceBuilder
import com.zephyr.net.bean.NetResult
import com.zephyr.net.requestEnqueue
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/*
v1

gemini-1.0-pro-vision-latest
gemini-pro-vision
gemini-1.5-pro-001
gemini-1.5-pro-002
gemini-1.5-pro
gemini-1.5-flash-001
gemini-1.5-flash-001-tuning
gemini-1.5-flash
gemini-1.5-flash-002
gemini-1.5-flash-8b
gemini-1.5-flash-8b-001
gemini-2.0-flash
gemini-2.0-flash-001
gemini-2.0-flash-lite-001
gemini-2.0-flash-lite
gemini-2.0-flash-preview-image-generation
embedding-001
text-embedding-004
 */

/*
v1beta

chat-bison-001
text-bison-001
embedding-gecko-001
gemini-1.0-pro-vision-latest
gemini-pro-vision
gemini-1.5-pro-latest
gemini-1.5-pro-001
gemini-1.5-pro-002
gemini-1.5-pro
gemini-1.5-flash-latest
gemini-1.5-flash-001
gemini-1.5-flash-001-tuning
gemini-1.5-flash
gemini-1.5-flash-002
gemini-1.5-flash-8b
gemini-1.5-flash-8b-001
gemini-1.5-flash-8b-latest
gemini-1.5-flash-8b-exp-0827
gemini-1.5-flash-8b-exp-0924
gemini-2.5-pro-exp-03-25
gemini-2.5-pro-preview-03-25
gemini-2.5-flash-preview-04-17
gemini-2.5-flash-preview-04-17-thinking
gemini-2.5-pro-preview-05-06
gemini-2.0-flash-exp
gemini-2.0-flash
gemini-2.0-flash-001
gemini-2.0-flash-exp-image-generation
gemini-2.0-flash-lite-001
gemini-2.0-flash-lite
gemini-2.0-flash-preview-image-generation
gemini-2.0-flash-lite-preview-02-05
gemini-2.0-flash-lite-preview
gemini-2.0-pro-exp
gemini-2.0-pro-exp-02-05
gemini-exp-1206
gemini-2.0-flash-thinking-exp-01-21
gemini-2.0-flash-thinking-exp
gemini-2.0-flash-thinking-exp-1219
learnlm-1.5-pro-experimental
learnlm-2.0-flash-experimental
gemma-3-1b-it
gemma-3-4b-it
gemma-3-12b-it
gemma-3-27b-it
embedding-001
text-embedding-004
gemini-embedding-exp-03-07
gemini-embedding-exp
aqa
 */

/*
v1alpha

chat-bison-001
text-bison-001
embedding-gecko-001
gemini-1.0-pro-vision-latest
gemini-pro-vision
gemini-1.5-pro-latest
gemini-1.5-pro-001
gemini-1.5-pro-002
gemini-1.5-pro
gemini-1.5-flash-latest
gemini-1.5-flash-001
gemini-1.5-flash-001-tuning
gemini-1.5-flash
gemini-1.5-flash-002
gemini-1.5-flash-8b
gemini-1.5-flash-8b-001
gemini-1.5-flash-8b-latest
gemini-1.5-flash-8b-exp-0827
gemini-1.5-flash-8b-exp-0924
gemini-2.5-pro-exp-03-25
gemini-2.5-pro-preview-03-25
gemini-2.5-flash-preview-04-17
gemini-2.5-flash-preview-04-17-thinking
gemini-2.5-pro-preview-05-06
gemini-2.0-flash-exp
gemini-2.0-flash
gemini-2.0-flash-001
gemini-2.0-flash-exp-image-generation
gemini-2.0-flash-lite-001
gemini-2.0-flash-lite
gemini-2.0-flash-preview-image-generation
gemini-2.0-flash-lite-preview-02-05
gemini-2.0-flash-lite-preview
gemini-2.0-pro-exp
gemini-2.0-pro-exp-02-05
gemini-exp-1206
gemini-2.0-flash-thinking-exp-01-21
gemini-2.0-flash-thinking-exp
gemini-2.0-flash-thinking-exp-1219
learnlm-1.5-pro-experimental
learnlm-2.0-flash-experimental
gemma-3-1b-it
gemma-3-4b-it
gemma-3-12b-it
gemma-3-27b-it
embedding-001
text-embedding-004
gemini-embedding-exp-03-07
gemini-embedding-exp
aqa
 */

data class ListModelsResponse(
    val models: List<Model>
)

data class Model(
    val name: String,
    @SerializedName("supportedGenerationMethods")
    val supportedGenerationMethods: List<String>?
)

interface GenerativeLanguageApi {
    @GET("{ver}/models")
    fun listModels(
        @Path("ver") apiVersion: String,
        @Query("key") apiKey: String
    ): Call<ListModelsResponse>
}

class ListModelService {
    private val service by lazy { ServiceBuilder.create<GenerativeLanguageApi>("https://generativelanguage.googleapis.com/") }

    fun call(key: String, apiVersion: String, callback: (NetResult<ListModelsResponse>) -> Unit) {
        requestEnqueue(service.listModels(apiVersion, key), callback)
    }
}