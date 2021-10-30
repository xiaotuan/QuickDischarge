package com.qty.quickdischarge

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class NetworkConnection(
    private val context: Context
) {

    private var mNetworkConnectionThread: Thread? = null

    private var isStarted = false

    fun start() {
        if (!isStarted) {
            isStarted = true
            mNetworkConnectionThread = Thread {
                while (isStarted) {
                    try {
                        val baidu = URL(context.resources.getString(R.string.network_connection_url))
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
                        Thread.sleep(context.resources.getInteger(R.integer.network_request_interval).toLong())
                    } catch (e: Exception) {
                        Log.e(TAG, "start=>error: $e")
                    }
                }
            }
            mNetworkConnectionThread!!.start()
        }
    }

    fun stop() {
        if (isStarted) {
            isStarted = false
            try {
                mNetworkConnectionThread?.interrupt()
            } catch (e: Exception) {
                Log.e(TAG, "stop=> $e")
            }
        }
    }

    companion object {
        const val TAG = "NetworkConnection"
    }

}