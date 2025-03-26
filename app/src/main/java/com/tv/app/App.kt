package com.tv.app

import android.app.Application
import com.zephyr.log.LogLevel
import com.zephyr.log.Logger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.startLogger(this, LogLevel.VERBOSE)
//        DynamicColors.applyToActivitiesIfAvailable(this)

//        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
//        } else {
//            getSystemService(VIBRATOR_SERVICE) as Vibrator
//        }
    }
}