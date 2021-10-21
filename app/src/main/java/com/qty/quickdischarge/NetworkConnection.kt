package com.qty.quickdischarge

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class NetworkConnection {

    private var mNetworkConnectionThread: Thread? = null

    private var isStarted = false

    public fun start() {
        if (!isStarted) {
            isStarted = true
            mNetworkConnectionThread = Thread {
                while (isStarted) {
                    try {
                        val baidu = URL("https://www.baidu.com/")
                        val connection = baidu.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.useCaches = false
                        connection.doInput = true
                        connection.doOutput = false
                        connection.connect()
                        Log.d(TAG, "start=>response code: ${connection.responseCode}")
                        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                            val sb = StringBuilder()
                            val reader = BufferedReader(InputStreamReader(connection.inputStream))
                            var line = reader.readLine()
                            while (line != null) {
                                sb.append(line)
                                line = reader.readLine()
                            }
                            Log.d(TAG, "start=>response: $sb")
                        }
                        connection.disconnect()
                        Thread.sleep(100)
                    } catch (e: Exception) {
                        Log.e(TAG, "start=>error: $e")
                    }
                }
            }
            mNetworkConnectionThread!!.start()
        }
    }

    public fun stop() {
        if (isStarted) {
            isStarted = false
            try {
                mNetworkConnectionThread!!.interrupt()
            } catch (e: Exception) {
                Log.e(TAG, "stop=> $e")
            }
        }
    }

    companion object {
        const val TAG = "NetworkConnection"
    }

}