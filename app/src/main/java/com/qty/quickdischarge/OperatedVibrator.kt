package com.qty.quickdischarge

import android.content.Context
import android.os.Vibrator

class OperatedVibrator(
    val context: Context
) {

    private val mVibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var isStarted = false

    public fun start() {
        if (!isStarted) {
            isStarted = true
            mVibrator.vibrate(longArrayOf(0, 1000, 0, 1000), 1)
        }
    }

    public fun stop() {
        if (isStarted) {
            isStarted = false
            mVibrator.cancel()
        }
    }

}