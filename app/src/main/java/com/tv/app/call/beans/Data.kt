package com.tv.app.call.beans

import com.google.gson.annotations.SerializedName

sealed class Data {
    data class Transcription(
        @SerializedName("serverContent") val serverContent: ServerContent?
    ) : Data()

    data class Blob(
        @SerializedName("serverContent") val serverContent: ServerContent?
    ) : Data()

    data class TurnComplete(
        @SerializedName("serverContent") val serverContent: ServerContent?
    ) : Data()

    data object SetupComplete : Data()

    // bean
    data class ServerContent(
        @SerializedName("modelTurn") val modelTurn: ModelTurn?,
        @SerializedName("turnComplete") val turnComplete: Boolean?,
        @SerializedName("outputTranscription") val outputTranscription: OutputTranscription?
    )

    data class OutputTranscription(
        @SerializedName("text") val text: String?
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

