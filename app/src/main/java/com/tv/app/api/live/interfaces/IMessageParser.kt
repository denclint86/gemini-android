package com.tv.app.api.live.interfaces

import com.tv.app.api.live.beans.ParsedResult
import okio.ByteString

// 消息解析接口
interface IMessageParser {
    var isSetupComplete: Boolean

    fun parseTextMessage(text: String): ParsedResult?
    fun parseBinaryMessage(bytes: ByteString): ParsedResult?
}