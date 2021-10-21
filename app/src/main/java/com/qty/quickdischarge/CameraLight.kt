package com.qty.quickdischarge

import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.lang.Exception

class CameraLight {

    private var mCamera: Camera? = null
    private var isStarted = false

    public fun start(holder: SurfaceHolder) {
        Log.d(TAG, "start=>isStarted: $isStarted")
        if (!isStarted) {
            isStarted = true
            startPreview(holder)
        }
    }

    public fun stop() {
        if (isStarted) {
            isStarted = false
            stopPreview()
        }
    }

    private fun startPreview(holder: SurfaceHolder) {
        val cameraId = getBackCameraId()
        mCamera = if (cameraId == -1) {
            Camera.open()
        } else {
            Camera.open(cameraId)
        }
        mCamera?.let {
            it.setPreviewDisplay(holder)
            var param = it.parameters
            param.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            it.parameters = param
            it.startPreview()
        }
    }

    private fun stopPreview() {
        mCamera?.let {
            it.stopPreview()
            it.release()
            mCamera = null
        }
    }

    private fun getBackCameraId(): Int {
        val count = Camera.getNumberOfCameras()
        var cameraInfo = Camera.CameraInfo()
        var i = 0
        while (i < count) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i
            }
            i++
        }
        return -1
    }

    companion object {
        const val TAG = "CameraLight"
    }
}