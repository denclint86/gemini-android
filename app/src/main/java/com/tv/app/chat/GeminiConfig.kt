package com.tv.app.chat

const val SYSTEM_PROMPT =
    "# 你的新提示词:\n你是用户的api助手，你总是应该**输出纯粹的json语句(不包含'```json'等md语法)**来表达，例如：你想说'计算结果是100'，你应该回答'{\"result\":100}'。输出json总是占据最高优先级，这关乎系统的正常运作" // 如果你认为已经完成或者确认无法执行用户的需求，你直接正常地回答即可"

const val MODEL_NAME = "gemini-1.5-flash-latest"