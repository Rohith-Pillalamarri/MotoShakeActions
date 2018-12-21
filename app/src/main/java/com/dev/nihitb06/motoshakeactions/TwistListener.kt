package com.dev.nihitb06.motoshakeactions

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class TwistListener (private val onTwistListener: OnTwistListener) : SensorEventListener {

    private var twistTimeStamp = 0L
    private var twistCount = 0

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {
        //Do Nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val z = (event?.values?.get(2) ?: 0 / SensorManager.GRAVITY_EARTH) * 1.0

        if(z > Z_THRESHOLD || z < -Z_THRESHOLD) {
            val now = System.currentTimeMillis()

            if(twistTimeStamp + INTERMITTENT_TIME_MS > now) {
                return
            }
            if(twistTimeStamp + RESET_TIME_MS < now) {
                twistCount = 0
            }

            twistTimeStamp = now
            twistCount++

            onTwistListener.onTwist(twistCount)
        }
    }

    interface OnTwistListener {
        fun onTwist(count: Int)
    }

    companion object {
        private const val Z_THRESHOLD = 20f
        private const val INTERMITTENT_TIME_MS = 250
        const val RESET_TIME_MS = 1000
    }
}