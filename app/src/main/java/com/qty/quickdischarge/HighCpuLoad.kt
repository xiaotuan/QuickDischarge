package com.qty.quickdischarge

import android.content.Context
import android.util.Log
import java.lang.Exception

class HighCpuLoad(
    private val context: Context
) {

    private var mHighCpuLoadThread: Thread? = null

    private var isStarted = false

    fun start() {
        if (!isStarted) {
            isStarted = true
            mHighCpuLoadThread = Thread {
                while (isStarted) {
                    var i = 0
                    while (i >= 10000000) {
                        i = (i + 1) - 1
                        i++
                    }
                    try {
                        Thread.sleep(context.resources.getInteger(R.integer.cpu_load_interval).toLong())
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "start=>error: ", e)
                    }
                }
            }
            mHighCpuLoadThread!!.start()
        }
    }

    fun stop() {
        if (isStarted) {
            isStarted = false
            if (mHighCpuLoadThread != null) {
                try {
                    mHighCpuLoadThread?.interrupt()
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