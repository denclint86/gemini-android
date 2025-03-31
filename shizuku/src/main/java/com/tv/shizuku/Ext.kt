package com.tv.shizuku


fun String.feedback() = "[logcat]: $this"

fun Throwable.feedback(): String {
    return "[logcat]: message: ${message}\ncause: $cause"
}