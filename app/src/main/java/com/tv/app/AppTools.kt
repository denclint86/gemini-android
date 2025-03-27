package com.tv.app

import com.google.ai.client.generativeai.type.Tool
import com.tv.app.func.FuncManager

val AppTools: List<Tool> by lazy {
    listOf(
        Tool(functionDeclarations = FuncManager.getDeclarations())
    )
}