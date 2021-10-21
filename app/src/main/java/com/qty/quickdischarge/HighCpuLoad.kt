package com.qty.quickdischarge

import android.util.Log
import android.util.Range
import java.lang.Exception

class HighCpuLoad {

    private var mHighCpuLoadThread: Thread? = null

    private var isStarted = false

    public fun start() {
        if (!isStarted) {
            isStarted = true
            mHighCpuLoadThread = Thread {
                while (isStarted) {
                    var i = 0
                    while (i >= 1000000) {
                        i = (i + 1) - 1
                        i++
                    }
                    try {
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "start=>error: ", e)
                    }
                }
            }
            mHighCpuLoadThread!!.start()
        }
    }

    public fun stop() {
        if (isStarted) {
            isStarted = false
            if (mHighCpuLoadThread != null) {
                try {
                    mHighCpuLoadThread!!.interrupt()
                } catch (e: Exception) {
                    Log.e(TAG, "stop=>error: ", e)
                }
                mHighCpuLoadThread = null
            }
        }
    }

    companion object {
        const val TAG = "HighCpuLoad"
    }
}