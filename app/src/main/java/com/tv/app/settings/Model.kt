package com.tv.app.settings

enum class Model(val string: String) {
    GEMINI_2_5_FLASH_PREVIEW_04_17("gemini-2.5-flash-preview-04-17"), // 支持函数调用，实验性，性价比高
    GEMINI_2_5_PRO_PREVIEW("gemini-2.5-pro-preview-03-25"), // 支持函数调用，实验性，高级推理
    GEMINI_2_5_PRO_EXP("gemini-2.5-pro-exp-03-25"), // 支持函数调用，实验性，高级推理
    GEMINI_2_0_FLASH("gemini-2.0-flash"), // 支持函数调用，稳定版，新一代功能
    GEMINI_2_0_FLASH_LITE("gemini-2.0-flash-lite"), // 支持函数调用，优化性价比和低延迟
    GEMINI_1_5_FLASH("gemini-1.5-flash"), // 支持函数调用，快速多模态
    GEMINI_1_5_FLASH_8B("gemini-1.5-flash-8b"), // 支持函数调用，小型模型，低智能任务
    GEMINI_1_5_PRO("gemini-1.5-pro"), // 支持函数调用，复杂推理任务
    GEMINI_EMBEDDING_EXP("gemini-embedding-exp-03-07"), // 不支持函数调用，文本嵌入
    IMAGEN_3("imagen-3.0-generate-002"), // 不支持函数调用，图片生成
    VEO_2("veo-2.0-generate-001"), // 不支持函数调用，视频生成
    GEMINI_2_0_FLASH_LIVE("gemini-2.0-flash-live-001"), // 支持函数调用，低延迟双向语音/视频
    TEXT_EMBEDDING_004("text-embedding-004"), // 不支持函数调用，文本嵌入
    EMBEDDING_001("embedding-001"), // 不支持函数调用，文本嵌入
    AQA("aqa") // 不支持函数调用，归因式问答
}