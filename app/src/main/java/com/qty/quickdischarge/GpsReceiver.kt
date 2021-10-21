package com.qty.quickdischarge

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

class GpsReceiver(
    private val context: Context
) {

    private val mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var isStarted = false

    private val mLocationChangeListener =
        LocationListener { location ->
            Log.d(TAG, "onLocationChanged=>latitude: ${location.latitude}, longitude: ${location.longitude}")
        }

    @SuppressLint("WrongConstant")
    public fun start() {
        if (!isStarted) {
            isStarted = true
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_FINE
                criteria.isAltitudeRequired = true
                criteria.isBearingRequired = true
                criteria.isCostAllowed = true
                criteria.powerRequirement = Criteria.POWER_HIGH
                criteria.isSpeedRequired = true
                val provider = mLocationManager.getBestProvider(criteria, true)!!

            } else {
                Toast.makeText(context, "请先打开定位", Toast.LENGTH_SHORT).show()
            }
        }
    }

    public fun stop() {
        if (isStarted) {
            isStarted = false
            mLocationManager.removeUpdates(mLocationChangeListener)
        }
    }

    companion object {
        const val TAG = "GpsReceiver"
    }
}