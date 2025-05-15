package com.tv.app.api.modellist.beans

import com.google.gson.annotations.SerializedName

/*
  {
        "name": "models/chat-bison-001",
        "version": "001",
        "displayName": "PaLM 2 Chat (Legacy)",
        "description": "A legacy text-only model optimized for chat conversations",
        "inputTokenLimit": 4096,
        "outputTokenLimit": 1024,
        "supportedGenerationMethods": [
          "generateMessage",
          "countMessageTokens"
        ],
        "temperature": 0.25,
        "topP": 0.95,
        "topK": 40
  }
 */
data class Model(
    val name: String?,
    @SerializedName("supportedGenerationMethods")
    val supportedGenerationMethods: List<String>?
)