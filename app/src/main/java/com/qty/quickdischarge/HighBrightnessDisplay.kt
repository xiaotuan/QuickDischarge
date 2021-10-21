package com.qty.quickdischarge

import android.content.Context
import android.os.PowerManager
import android.provider.Settings

class HighBrightnessDisplay(
    val context: Context
) {

    private var mLastBrightness = -1

    public fun start() {
        mLastBrightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1)
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 255)
    }

    public fun stop() {
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, mLastBrightness)
    }

}