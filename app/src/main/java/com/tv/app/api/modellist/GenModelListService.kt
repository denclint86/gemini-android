package com.tv.app.api.modellist

import com.tv.app.api.modellist.beans.ListModelsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GenModelListService {
    @GET("{ver}/models")
    fun listModels(
        @Path("ver") apiVersion: String,
        @Query("key") apiKey: String
    ): Call<ListModelsResponse>
}