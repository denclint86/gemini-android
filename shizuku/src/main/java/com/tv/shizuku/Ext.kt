package com.tv.shizuku


fun String.feedbackStr() = "[logcat]: $this"

fun Throwable.feedback(): String {
    return "[logcat]: message: ${message}\ncause: $cause"
}