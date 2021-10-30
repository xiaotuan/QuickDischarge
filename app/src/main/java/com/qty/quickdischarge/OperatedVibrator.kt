package com.qty.quickdischarge

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class OperatedVibrator(
    private val context: Context
) {

    private var mVibrator: Vibrator? = null
    private var isStarted = false

    fun start() {
        if (!isStarted) {
            isStarted = true
            mVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val intPattern = context.resources.getIntArray(R.array.vibrator_pattern)
            var pattern = arrayListOf<Long>()
            for (i in intPattern) {
                pattern.add(i.toLong())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mVibrator?.vibrate(VibrationEffect.createWaveform(pattern.toLongArray(), 1))
            } else {
                mVibrator?.vibrate(pattern.toLongArray(), 1)
            }
        }
    }

    fun stop() {
        if (isStarted) {
            isStarted = false
            mVibrator?.cancel()
        }
    }

}