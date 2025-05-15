package com.tv.app.api.live

import com.google.gson.annotations.SerializedName

data class ClientContentMessage(
    @SerializedName("client_content") val clientContent: ClientContent
) {
    data class ClientContent(
        val turns: List<Turn>,
        @SerializedName("turn_complete") val turnComplete: Boolean
    ) {
        data class Turn(
            val role: String,
            val parts: List<Part>
        ) {
            constructor(role: String, msg: String) : this(
                role = role,
                parts = listOf(Part(msg))
            )

            data class Part(
                val text: String
            )
        }
    }
}