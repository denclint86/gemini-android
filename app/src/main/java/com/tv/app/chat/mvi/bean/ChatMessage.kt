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

package com.tv.app.chat.mvi.bean

import com.tv.app.chat.Role
import java.util.UUID

/**
 * 谷歌封装的 ui 数据类
 */
data class ChatMessage(
    val id: UUID = UUID.randomUUID(),
    val text: String = "",
    val role: Role = Role.USER,
    val isPending: Boolean = false
)

fun userMsg(text: String, isPending: Boolean = false) =
    ChatMessage(text = text, role = Role.USER, isPending = isPending)

fun modelMsg(text: String, isPending: Boolean = false) =
    ChatMessage(text = text, role = Role.MODEL, isPending = isPending)

fun funcMsg(text: String, isPending: Boolean) =
    ChatMessage(text = text, role = Role.FUNC, isPending = isPending)

fun systemMsg(text: String) =
    ChatMessage(text = text, role = Role.SYSTEM, isPending = false)
