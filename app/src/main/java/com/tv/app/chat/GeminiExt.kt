package com.tv.app.chat

import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content

fun userContent(init: Content.Builder.() -> Unit) = content(role = Role.USER.str, init)
//fun systemContent(init: Content.Builder.() -> Unit) = content(role = Role.SYSTEM.str, init)
fun modelContent(init: Content.Builder.() -> Unit) = content(role = Role.MODEL.str, init)
