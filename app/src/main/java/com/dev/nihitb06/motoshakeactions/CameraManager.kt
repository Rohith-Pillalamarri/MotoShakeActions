package com.dev.nihitb06.motoshakeactions

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager

class CameraManager (context: Context) {

    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList[0]

    fun toggleFlash(toggle: Boolean) {
        try {
            cameraManager.setTorchMode(cameraId, toggle)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}