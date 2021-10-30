package com.qty.quickdischarge

import android.content.Context
import android.provider.Settings

class HighBrightnessDisplay(
    private val context: Context
) {

    private var mLastBrightness = -1
    private var isStarted = false

    fun start() {
        if (!isStarted) {
            isStarted = true
            mLastBrightness = Settings.System.getInt(context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                -1)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 255)
        }
    }

    fun stop() {
        if (isStarted) {
            isStarted = false
            Settings.System.putInt(context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                mLastBrightness)
        }
    }

}