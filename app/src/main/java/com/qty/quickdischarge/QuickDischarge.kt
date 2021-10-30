package com.qty.quickdischarge

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import java.lang.Exception
import kotlin.math.round

class QuickDischarge : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, SurfaceHolder.Callback {

    private lateinit var mTitleTv: TextView
    private lateinit var mSurfaceView: SurfaceView
    private lateinit var mContainer: View
    private lateinit var mBatteryPowerTv: TextView
    private lateinit var mTechnologyTv: TextView
    private lateinit var mTemperatureTv: TextView
    private lateinit var mStatusTv: TextView
    private lateinit var mPluggedTv: TextView
    private lateinit var mHealthTv: TextView
    private lateinit var mVoltageTv: TextView
    private lateinit var mGoalsBatteryTv: TextView
    private lateinit var mBatteryLevelSb: SeekBar
    private lateinit var mHighCpuLoadCb: CheckBox
    private lateinit var mCameraLightCb: CheckBox
    private lateinit var mHighBrightnessDisplayCb: CheckBox
    private lateinit var mGpsReceiversCb: CheckBox
    private lateinit var mOperatedVibratorCb: CheckBox
    private lateinit var mNetworkConnectionCb: CheckBox
    private lateinit var mDischargingTb: ToggleButton

    private lateinit var mBatteryChangeReceiver: BatteryChangedReceiver
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mHighCpuLoad: HighCpuLoad
    private lateinit var mCameraLight: CameraLight
    private lateinit var mHighBrightnessDisplay: HighBrightnessDisplay
    private lateinit var mGpsReceiver: GpsReceiver
    private lateinit var mOperatedVibrator: OperatedVibrator
    private lateinit var mNetworkConnection: NetworkConnection
    private var mHolder: SurfaceHolder? = null

    private var mBatteryLevel = -1
    private var mTechnology = ""
    private var mTemperature = 0
    private var mStatus = 0
    private var mPlugged = 0
    private var mHealth = BatteryManager.BATTERY_HEALTH_UNKNOWN
    private var mVoltage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.BLACK
        }

        mTitleTv = findViewById(R.id.app_name)
        mSurfaceView = findViewById(R.id.surface_view)
        mContainer = findViewById(R.id.container)
        mBatteryPowerTv = findViewById(R.id.battery_power)
        mTechnologyTv = findViewById(R.id.technology)
        mTemperatureTv = findViewById(R.id.temperature)
        mStatusTv = findViewById(R.id.status)
        mPluggedTv = findViewById(R.id.plugged)
        mHealthTv = findViewById(R.id.health)
        mVoltageTv = findViewById(R.id.voltage)
        mGoalsBatteryTv = findViewById(R.id.goals_battery)
        mBatteryLevelSb = findViewById(R.id.level_seekbar)
        mHighCpuLoadCb = findViewById(R.id.high_cpu_load)
        mCameraLightCb = findViewById(R.id.camera_light)
        mHighBrightnessDisplayCb = findViewById(R.id.high_brightness_display)
        mGpsReceiversCb = findViewById(R.id.gps_receivers)
        mOperatedVibratorCb = findViewById(R.id.operateed_vibrator)
        mNetworkConnectionCb = findViewById(R.id.network_connection)
        mDischargingTb = findViewById(R.id.discharging)

        mHighCpuLoad = HighCpuLoad(this)
        mCameraLight = CameraLight()
        mHighBrightnessDisplay = HighBrightnessDisplay(this)
        mGpsReceiver = GpsReceiver(this)
        mOperatedVibrator = OperatedVibrator(this)
        mNetworkConnection = NetworkConnection(this)
        mBatteryChangeReceiver = BatteryChangedReceiver(this)
        registerReceiver(mBatteryChangeReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            title = "${getString(R.string.app_name)} Ver ${packageInfo.versionName}"
        } catch (e: Exception) {
            Log.e(TAG, "onCreate=>error: ", e)
        }

        mSurfaceView.holder.addCallback(this)

        mBatteryLevelSb.setOnSeekBarChangeListener(this)

        mDischargingTb.setOnClickListener(this)

        mHighCpuLoadCb.setOnCheckedChangeListener(this)
        mCameraLightCb.setOnCheckedChangeListener(this)
        mHighBrightnessDisplayCb.setOnCheckedChangeListener(this)
        mGpsReceiversCb.setOnCheckedChangeListener(this)
        mOperatedVibratorCb.setOnCheckedChangeListener(this)
        mNetworkConnectionCb.setOnCheckedChangeListener(this)

    }

    override fun onResume() {
        super.onResume()
        updateAll()
    }

    override fun onPause() {
        super.onPause()
        if (mDischargingTb.isChecked) {
            stopDisCharge()
            mDischargingTb.isChecked = false
        }
    }

    override fun onDestroy() {
        mSurfaceView.holder.removeCallback(this)
        unregisterReceiver(mBatteryChangeReceiver)
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDischargingTb.isChecked) {
                AlertDialog.Builder(this)
                    .setMessage(R.string.exit_tip)
                    .setPositiveButton(R.string.sure) { _, _ ->
                        finish()
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .create()
                    .show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (progress > mBatteryLevel - 1) {
            mBatteryLevelSb.progress = mBatteryLevel - 1
        }
        mGoalsBatteryTv.text = getString(R.string.goals_battery, "${mBatteryLevelSb.progress}%")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick=>check: ${mDischargingTb.isChecked}, need: ${needObtainPermission()}")
        if (!needObtainPermission()) {
            if (mDischargingTb.isChecked) {
                startDisCharge()
            } else {
                stopDisCharge()
            }
        } else {
            mDischargingTb.isChecked = false
            obtainPermission()
        }
        updateDischargingStatus()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUET_CODE) {
            var isGrantAll = true
            var index = 0
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult=>${permissions[index]} denied.")
                    isGrantAll = false
                    break
                }
                index++
            }
            if (!isGrantAll) {
                Toast.makeText(this, "权限拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        buttonView?.let {
            when (it.id) {
                R.id.high_cpu_load -> mSharedPreferences.edit().putBoolean(Constant.HIGH_CPU_LOAD_KEY, isChecked).apply()
                R.id.camera_light -> mSharedPreferences.edit().putBoolean(Constant.CAMERA_LIGHT_KEY, isChecked).apply()
                R.id.high_brightness_display -> mSharedPreferences.edit().putBoolean(Constant.HIGH_BRIGHTNESS_DISPLAY_KEY, isChecked).apply()
                R.id.gps_receivers -> mSharedPreferences.edit().putBoolean(Constant.GPS_RECEIVERS_KEY, isChecked).apply()
                R.id.operateed_vibrator -> mSharedPreferences.edit().putBoolean(Constant.OPERATED_VIBRATOR_KEY, isChecked).apply()
                R.id.network_connection -> mSharedPreferences.edit().putBoolean(Constant.NETWORK_CONNECTION_KEY, isChecked).apply()
            }
            updateDischargingButtonStatus()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated=>holder: $holder")
        mHolder = holder
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged=>holder: $holder, format: $format, width: $width, height: $height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed=>holder: $holder")
        mHolder = null
        mCameraLight.stop()
    }

    private fun updateAll() {
        mGoalsBatteryTv.text = getString(R.string.goals_battery, "${mBatteryLevelSb.progress}%")
        updateDischargingStatus()
        updateBatteryInfo()
        updateCheckboxStatus()
        updateDischargingButtonStatus()
    }

    private fun updateDischargingStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mDischargingTb.isChecked) {
                mContainer.setBackgroundColor(getColor(android.R.color.background_light))
            } else {
                mContainer.setBackgroundColor(getColor(android.R.color.background_dark))
            }
        } else {
            if (mDischargingTb.isChecked) {
                mContainer.setBackgroundColor(resources.getColor(android.R.color.background_light))
            } else {
                mContainer.setBackgroundColor(resources.getColor(android.R.color.background_dark))
            }
        }
        setViewEnabled(!mDischargingTb.isChecked)
    }

    private fun setViewEnabled(enabled: Boolean) {
        mBatteryLevelSb.isEnabled = enabled
        mHighCpuLoadCb.isEnabled = enabled
        mCameraLightCb.isEnabled = enabled
        mHighBrightnessDisplayCb.isEnabled = enabled
        mGpsReceiversCb.isEnabled = enabled
        mOperatedVibratorCb.isEnabled = enabled
        mNetworkConnectionCb.isEnabled = enabled
    }

    private fun updateBatteryInfo() {
        mBatteryPowerTv.text = getString(R.string.battery_power, mBatteryLevel)
        mTechnologyTv.text = getString(R.string.technology, mTechnology)
        mTemperatureTv.text = getString(R.string.temperature, mTemperature / 10, (mTemperature / 10 * 1.8 + 32).toInt())
        mStatusTv.text = getString(R.string.status, getStatusDescription())
        mPluggedTv.text = getString(R.string.plugged, getPluggedDescription())
        mHealthTv.text = getString(R.string.health, getHealthDescription())
        mVoltageTv.text = getString(R.string.voltage, mVoltage)
        when {
            mBatteryLevel <= 25 -> {
                mBatteryPowerTv.setTextColor(Color.RED)
            }
            mBatteryLevel <= 50 -> {
                mBatteryPowerTv.setTextColor(Color.YELLOW)
            }
            else -> {
                mBatteryPowerTv.setTextColor(Color.GREEN)
            }
        }
    }

    private fun updateCheckboxStatus() {
        mHighCpuLoadCb.isChecked = mSharedPreferences.getBoolean(Constant.HIGH_CPU_LOAD_KEY, false)
        mCameraLightCb.isChecked = mSharedPreferences.getBoolean(Constant.CAMERA_LIGHT_KEY, false)
        mHighBrightnessDisplayCb.isChecked = mSharedPreferences.getBoolean(Constant.HIGH_BRIGHTNESS_DISPLAY_KEY, false)
        mGpsReceiversCb.isChecked = mSharedPreferences.getBoolean(Constant.GPS_RECEIVERS_KEY, false)
        mOperatedVibratorCb.isChecked = mSharedPreferences.getBoolean(Constant.OPERATED_VIBRATOR_KEY, false)
        mNetworkConnectionCb.isChecked = mSharedPreferences.getBoolean(Constant.NETWORK_CONNECTION_KEY, false)
    }

    private fun updateDischargingButtonStatus() {
        val highCpuLoad = mSharedPreferences.getBoolean(Constant.HIGH_CPU_LOAD_KEY, false)
        val cameraLight = mSharedPreferences.getBoolean(Constant.CAMERA_LIGHT_KEY, false)
        val highBrightnessDisplay = mSharedPreferences.getBoolean(Constant.HIGH_BRIGHTNESS_DISPLAY_KEY, false)
        val gpsReceivers = mSharedPreferences.getBoolean(Constant.GPS_RECEIVERS_KEY, false)
        val operatedVibrator = mSharedPreferences.getBoolean(Constant.OPERATED_VIBRATOR_KEY, false)
        val networkConnection = mSharedPreferences.getBoolean(Constant.NETWORK_CONNECTION_KEY, false)
        mDischargingTb.isEnabled = highCpuLoad || cameraLight || highBrightnessDisplay || gpsReceivers || operatedVibrator || networkConnection
    }

    private fun obtainPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUET_CODE)
    }

    private fun needObtainPermission(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                return true
            }
        }

        return false
    }

    private fun getStatusDescription(): String {
        return when (mStatus) {
            BatteryManager.BATTERY_STATUS_CHARGING -> getString(R.string.battery_status_charging)
            BatteryManager.BATTERY_STATUS_DISCHARGING -> getString(R.string.battery_status_discharging)
            BatteryManager.BATTERY_STATUS_FULL -> getString(R.string.battery_status_full)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> getString(R.string.battery_status_not_charging)
            else -> getString(R.string.unknow)
        }
    }

    private fun getPluggedDescription(): String {
        return when (mPlugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> getString(R.string.battery_plugged_ac)
            BatteryManager.BATTERY_PLUGGED_USB -> getString(R.string.battery_plugged_usb)
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> getString(R.string.battery_plugged_wireless)
            else -> getString(R.string.unknow)
        }
    }

    private fun getHealthDescription(): String {
        return when (mHealth) {
            BatteryManager.BATTERY_HEALTH_COLD -> getString(R.string.battery_health_cold)
            BatteryManager.BATTERY_HEALTH_DEAD -> getString(R.string.battery_health_dead)
            BatteryManager.BATTERY_HEALTH_GOOD -> getString(R.string.battery_health_good)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> getString(R.string.battery_health_over_heat)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> getString(R.string.battery_health_over_voltage)
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> getString(R.string.battery_health_unspecified_failure)
            else -> getString(R.string.unknow)
        }
    }

    private fun startDisCharge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setBackgroundDrawable(resources.getDrawable(R.drawable.window_background_discharge_color))
            window.statusBarColor = resources.getColor(R.color.status_bar_bg)
        }
        mTitleTv.setTextColor(Color.GRAY)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (mHighCpuLoadCb.isChecked) {
            mHighCpuLoad.start()
        }
        if (mCameraLightCb.isChecked && mHolder != null) {
            mCameraLight.start(mHolder!!)
        }
        if (mHighBrightnessDisplayCb.isChecked) {
            mHighBrightnessDisplay.start()
        }
        if (mGpsReceiversCb.isChecked) {
            mGpsReceiver.start()
        }
        if (mOperatedVibratorCb.isChecked) {
            mOperatedVibrator.start()
        }
        if (mNetworkConnectionCb.isChecked) {
            mNetworkConnection.start()
        }
    }

    private fun stopDisCharge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setBackgroundDrawable(resources.getDrawable(R.drawable.window_background_normal_color))
            window.statusBarColor = Color.BLACK
        }
        mTitleTv.setTextColor(Color.WHITE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mHighCpuLoad.stop()
        mCameraLight.stop()
        mHighBrightnessDisplay.stop()
        mGpsReceiver.stop()
        mOperatedVibrator.stop()
        mNetworkConnection.stop()
    }

    private class BatteryChangedReceiver(
        val activity: QuickDischarge
    ): BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                Log.d(TAG, "onReceive=>action: ${it.action}")
                if (Intent.ACTION_BATTERY_CHANGED == it.action) {
                    if (activity.mBatteryLevel == -1) {
                        activity.mBatteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        activity.mBatteryLevelSb.progress = round(activity.mBatteryLevel * 0.5).toInt()
                    } else {
                        activity.mBatteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    }
                    val technology = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
                    activity.mTechnology = technology ?: context!!.getString(R.string.unknow)
                    activity.mTemperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                    activity.mStatus = it.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                    activity.mPlugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    activity.mHealth = it.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                    activity.mVoltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                    Log.d(TAG, "onReceive=>level: ${activity.mBatteryLevel}, technology: " +
                            "$technology, temperature: ${activity.mTemperature}, status: ${activity.mStatus}, " +
                            "plugged: ${activity.mPlugged}, health: ${activity.mHealth}, voltage: ${activity.mVoltage}")
                    activity.updateAll()
                }
            }
        }

    }

    companion object {
        const val TAG = "QuickDischarge"
        const val REQUET_CODE = 666
        val PERMISSIONS = arrayOf(Manifest.permission.INTERNET, Manifest.permission.VIBRATE, Manifest.permission.WAKE_LOCK,
            Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
}