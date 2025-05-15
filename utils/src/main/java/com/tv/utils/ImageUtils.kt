package com.tv.utils

import android.graphics.Bitmap
import android.media.Image
import android.util.DisplayMetrics

fun resizeBitmap(bitmap: Bitmap, scaleFactor: Float): Bitmap {
    val newWidth = (bitmap.width * scaleFactor).toInt()
    val newHeight = (bitmap.height * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun Image.toBitmap(metrics: DisplayMetrics): Bitmap? {
    val planes = planes.takeIf { it.isNotEmpty() } ?: return null
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * metrics.widthPixels

    return Bitmap.createBitmap(
        metrics.widthPixels + rowPadding / pixelStride,
        metrics.heightPixels,
        Bitmap.Config.ARGB_8888
    ).apply {
        copyPixelsFromBuffer(buffer)
    }.let {
        Bitmap.createBitmap(it, 0, 0, metrics.widthPixels, metrics.heightPixels)
    }
}