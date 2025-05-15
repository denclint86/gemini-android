package com.tv.app.view.suspendview.interfaces

import android.content.Intent
import android.graphics.Bitmap

interface ICaptureManager {
    val isAvailable: Boolean

    fun setup(resultCode: Int, data: Intent)
    suspend fun capture(): Bitmap?
    fun release()
}