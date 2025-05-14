package com.tv.app.settings.values

object Names {
    const val INDEX = "api_key 索引"
    const val SLEEP_TIME = "调用冷却时间"
    const val TIMEOUT = "网络请求超时(ms)"
    const val STREAM = "使用流式传输"

    const val API_VERSION = "api 版本"

    const val MODEL_NAME = "模型"
    const val SYSTEM_PROMPT = "提示词"

    const val TEMPERATURE = "温度"
    const val TOOLS = "函数调用"
    const val LIVE = "实时通话(启用后其他设置失效, 并且仅支持单轮对话)"
    const val MAX_OUTPUT_TOKENS = "最大输出 tokens"
    const val TOP_P = "top_p"
    const val TOP_K = "top_k"
    const val CANDIDATE_COUNT = "候选回答"
    const val PRESENCE_PENALTY = "令牌惩罚"
    const val FREQUENCY_PENALTY = "频率惩罚"
}