package com.qty.quickdischarge

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

class GpsReceiver(
    private val context: QuickDischarge
) {

    private val mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var isStarted = false

    private val mLocationChangeListener = LocationListener { location ->
        Log.d(TAG, "onLocationChanged=>latitude: ${location.latitude}, longitude: ${location.longitude}")
        Toast.makeText(context, "latitude: ${location.latitude}, longitude: ${location.longitude}", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("WrongConstant")
    fun start() {
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
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(context, R.string.grant_location_permission_tip, Toast.LENGTH_SHORT).show()
                    return
                }
                mLocationManager.requestLocationUpdates(provider,
                    context.resources.getInteger(R.integer.gps_location_time_interval).toLong(),
                    context.resources.getInteger(R.integer.gps_distance).toFloat(),
                    mLocationChangeListener)
            } else {
                Toast.makeText(context, R.string.turn_on_gps_tip, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun stop() {
        if (isStarted) {
            isStarted = false
            mLocationManager.removeUpdates(mLocationChangeListener)
        }
    }

    companion object {
        const val TAG = "GpsReceiver"
    }
}