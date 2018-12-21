package com.dev.nihitb06.motoshakeactions

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeListener (private val onShakeListener: OnShakeListener) : SensorEventListener {

    private var shakeTimeStamp = 0L
    private var shakeCount = 0

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Do Nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val x = (event?.values?.get(0) ?: 0 / SensorManager.GRAVITY_EARTH) * 1.0
        val y = (event?.values?.get(1) ?: 0 / SensorManager.GRAVITY_EARTH) * 1.0

        val gForce = Math.sqrt(x*x + y*y)

        if(gForce > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()

            if(shakeTimeStamp + INTERMITTENT_TIME_MS > now) {
                return
            }
            if(shakeTimeStamp + RESET_TIME_MS < now) {
                shakeCount = 0
            }

            shakeTimeStamp = now
            shakeCount++

            onShakeListener.onShake(shakeCount)
        }
    }

    interface OnShakeListener {
        fun onShake(count: Int)
    }

    companion object {
        private const val SHAKE_THRESHOLD = 28f
        private const val INTERMITTENT_TIME_MS = 250
        private const val RESET_TIME_MS = 1000
    }
}