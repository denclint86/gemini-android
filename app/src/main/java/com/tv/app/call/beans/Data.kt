package com.tv.app.call.beans

import com.google.gson.annotations.SerializedName

sealed class Data {
    data class Blob(
        @SerializedName("serverContent") val serverContent: ServerContent?
    ) : Data() {
        data class ServerContent(
            @SerializedName("modelTurn") val modelTurn: ModelTurn?
        )

        data class ModelTurn(
            @SerializedName("parts") val parts: List<Part>?
        )

        data class Part(
            @SerializedName("inlineData") val inlineData: InlineData?
        )

        data class InlineData(
            @SerializedName("mimeType") val mimeType: String?,
            @SerializedName("data") val data: String?
        )
    }

    data class SetupComplete(
        @SerializedName("setupComplete") val isComplete: Boolean?
    ) : Data()
}

