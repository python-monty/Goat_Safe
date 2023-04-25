package com.cs523.android.means_v2

import android.app.*
import android.content.BroadcastReceiver
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class FallService: Service(), SensorEventListener {

    private lateinit var sensorManager : SensorManager
    private var accelerometer: Sensor? = null
    private var accelerationReader : Float = SensorManager.GRAVITY_EARTH

    private val CHANNEL_ID = "Goatsafe"
    private val CHANNEL_ID_NOTIFICATIONS = "Goatsafe_notifs"
    private val NOTIFICATION_ID = 8
    private var ct = 3

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                sensorManager.unregisterListener(this)
                stopSelf()
            }
            ACTION_SHOW_FALL_NOTIFICATION -> {
                val address = intent.getStringExtra("address") // Get the address from the intent
                showFallDetectionNotification(address) // Pass the address to the function
            }
            else -> setupSensor()
        }
        return START_STICKY
    }


    private fun setupSensor():Int {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //register accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        Log.d("foreground","FS53")

        startForegroundNotification()

        accelerometer?.also {
            sensorManager.registerListener(this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelName = "My Background Service"
            val chan = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan);

            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("FreeFallService")
                .setContentText("Foreground service is running")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build()
        } else {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FreeFallService")
                .setContentText("Foreground service is running")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build()
        }
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                accelerationReader = sqrt(
                    x.toDouble().pow(2.0) + y.toDouble().pow(2.0) + z.toDouble().pow(2.0)
                ).toFloat()

                val precision = DecimalFormat("0.00")
                val ldAccRound = java.lang.Double.parseDouble(precision.format(accelerationReader))

                val intent = Intent(ACTION_UPDATE_UI)
                //Log.d("sending","sending")
                intent.putExtra("acceleration", ldAccRound)
                // Use LocalBroadcastManager to send the broadcast
                Log.d("sending","sending")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                Log.d("sent","sent")
                //sendBroadcast(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("com.cs528.android.falldetection.SHOW_FALL_NOTIFICATION")
        registerReceiver(notificationReceiver, filter)
    }

    override fun onDestroy() {
        unregisterReceiver(notificationReceiver)
        super.onDestroy()
    }


    private val notificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val timeStamp = intent?.getStringExtra("timeStamp") ?: ""
            val duration = intent?.getStringExtra("duration") ?: ""
            showFallNotification(timeStamp, duration)
        }
    }

    private fun showFallNotification(timeStamp: String, duration: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelName = "My Background Service2"
            val chan = NotificationChannel(CHANNEL_ID_NOTIFICATIONS, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(chan);

            val builder = Notification.Builder(this, CHANNEL_ID_NOTIFICATIONS)
                .setContentTitle("FreeFallService")
                .setContentText("Last fall $timeStamp with duration of $duration ms")
                .setSmallIcon(R.drawable.ic_launcher_background)

            manager.notify(ct++, builder.build())

        }else{

            val builder = NotificationCompat.Builder(this, CHANNEL_ID_NOTIFICATIONS)
                .setContentTitle("FreeFallService")
                .setContentText("Last fall $timeStamp with duration of $duration ms")
                .setSmallIcon(R.drawable.ic_launcher_background)

            manager.notify(ct++, builder.build())
        }
    }

    companion object {
        const val ACTION_UPDATE_UI = "com.cs528.android.ACTION_UPDATE_UI"
        const val ACTION_STOP_SERVICE = "com.cs528.android.ACTION_STOP_SERVICE"
        const val ACTION_SHOW_FALL_NOTIFICATION = "com.cs528.android.SHOW_FALL_NOTIFICATION"
    }

    override fun onAccuracyChanged(event: Sensor?, p1: Int) {
        return
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // This method will be called if the service is removed from the recent apps list
        super.onTaskRemoved(rootIntent)
    }

    private fun showFallDetectionNotification(address: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a new intent for the FallAlert activity and add the address as an extra
        val fallAlertIntent = Intent(this, FallAlert::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("address", address)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, fallAlertIntent,PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "fall_detection_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Fall Detection", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fall Detected")
            .setContentText("Tap to open FallAlert activity")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

}