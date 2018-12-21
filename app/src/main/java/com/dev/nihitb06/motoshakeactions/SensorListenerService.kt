package com.dev.nihitb06.motoshakeactions

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.view.accessibility.AccessibilityEvent

class SensorListenerService : AccessibilityService () {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private lateinit var shakeListener: ShakeListener
    private lateinit var twistListener: TwistListener

    private lateinit var cameraManager: CameraManager
    private var isFlashLightOn = false

    @Volatile
    private var isActionInProgress = false

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        initializeListeners()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(twistListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(shakeListener)
        sensorManager.unregisterListener(twistListener)

        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //Do Nothing
    }

    override fun onInterrupt() {
        //Do Nothing
    }

    private fun initializeListeners() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        cameraManager = CameraManager(this)

        shakeListener = ShakeListener(object: ShakeListener.OnShakeListener {
            override fun onShake(count: Int) {
                if(count % 2 == 0 && !isActionInProgress) {
                    isActionInProgress = true

                    cameraManager.toggleFlash(!isFlashLightOn)
                    isFlashLightOn = !isFlashLightOn

                    vibrate()

                    with(NotificationManagerCompat.from(this@SensorListenerService)) {
                        if(isFlashLightOn) {
                            notify(NOTIFICATION_ID, buildNotification())
                        } else {
                            cancel(NOTIFICATION_ID)
                        }
                    }

                    isActionInProgress = false
                }
            }
        })
        twistListener = TwistListener(object: TwistListener.OnTwistListener {
            override fun onTwist(count: Int) {
                if(count >= 2 && !isActionInProgress) {
                    isActionInProgress = true
                    vibrate()
                    openCameraActivity()
                }
            }
        })
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    NotificationChannel(
                            CHANNEL_ID,
                            getString(R.string.channel_name),
                            NotificationManager.IMPORTANCE_LOW
                    ).apply { description = getString(R.string.channel_description) }
            )
        }
    }
    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_highlight_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setColorized(true)
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(true)
            .build()

    private fun openCameraActivity() {
        val cameraInfo = packageManager.resolveActivity(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), 0)
        startActivity(
                Intent()
                        .setAction(Intent.ACTION_MAIN)
                        .setComponent(ComponentName(cameraInfo.activityInfo.packageName, cameraInfo.activityInfo.name))
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

        isActionInProgress = false
    }

    @SuppressWarnings("deprecation")
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
        else
            vibrator.vibrate(250)
    }

    companion object {
        private const val CHANNEL_ID = "MotoShakeActionsChannel"
        private const val NOTIFICATION_ID = 2611
    }
}