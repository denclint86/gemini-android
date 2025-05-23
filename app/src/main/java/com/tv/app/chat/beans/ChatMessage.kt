/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tv.app.chat.beans

import com.google.ai.client.generativeai.type.Content
import com.tv.settings.getSetting
import com.tv.settings.intances.SystemPrompt
import com.tv.utils.ROLE
import com.tv.utils.Role
import com.tv.utils.toUIString
import java.util.UUID

/**
 * 谷歌封装的 ui 数据类
 */
data class ChatMessage(
    val id: UUID = UUID.randomUUID(),
    val text: String = "",
    val role: Role = Role.USER,
    val isPending: Boolean = false
) {
    companion object {
        fun fromContent(content: Content) = ChatMessage(
            text = content.toUIString(),
            role = content.ROLE,
            isPending = false
        )
    }
}

fun userMsg(text: String, isPending: Boolean = false) =
    ChatMessage(text = text, role = Role.USER, isPending = isPending)

fun modelMsg(text: String, isPending: Boolean = false) =
    ChatMessage(text = text, role = Role.MODEL, isPending = isPending)

fun funcMsg(text: String, isPending: Boolean) =
    ChatMessage(text = text, role = Role.FUNC, isPending = isPending)

fun systemMsg(text: String) =
    ChatMessage(text = text, role = Role.SYSTEM, isPending = false)

val testMsgList: List<ChatMessage> = listOf(
    ChatMessage(text = "SSSSaaaaa\nbbbbb\nccccc\nddddd", role = Role.SYSTEM),
    ChatMessage(text = "UUUUaaaaa\nbbbbb\nccccc\nddddd", role = Role.USER),
    ChatMessage(text = "MMMMaaaaa\nbbbbb\nccccc\nddddd", role = Role.MODEL),
    ChatMessage(text = "FFFFaaaaa\nbbbbb\nccccc\nddddd", role = Role.FUNC),
    ChatMessage(text = "MMMMaaaaa\nbbbbb\nccccc\nddddd", role = Role.MODEL)
)

val systemMsg: ChatMessage =
    systemMsg(getSetting<SystemPrompt>()?.value(true)!!)

val systemMsgList: List<ChatMessage> = listOf(systemMsg)