package my.paidchain.spectraterminaldemo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import my.paidchain.spectraterminaldemo.FullscreenActivity
import my.paidchain.spectraterminaldemo.R
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import java.util.Locale

enum class ServiceState {
    START,
    STOP
}

class IntentService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log(Level.INFO, javaClass.simpleName) { "onStartCommand executed with startId: $startId" }
        if (intent != null) {
            val action = intent.action
            log(Level.INFO, javaClass.simpleName) { "using an intent with action $action" }
            when (action) {
                ServiceState.START.name -> startService()
                ServiceState.STOP.name -> stopService()
                else -> log(Level.INFO, javaClass.simpleName) { "This should never happen. No action in the received intent" }
            }
        } else {
            log(Level.INFO, javaClass.simpleName) {
                "with a null intent. It has been probably restarted by the system."
            }
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log(Level.INFO, javaClass.simpleName) { "The service has been created".uppercase(Locale.ROOT) }
        val notification = createNotification()
        startForeground(1, notification)
        startService()
    }

    override fun onDestroy() {
        super.onDestroy()

        log(Level.INFO, javaClass.simpleName) { "The service has been terminated" }
        Toast.makeText(this, "Demo service has been terminated", Toast.LENGTH_SHORT).show()
    }

    private fun startService() {
        synchronized(this) {
            if (isServiceStarted) return
            isServiceStarted = true
        }

        log(Level.INFO, javaClass.simpleName) { "Starting the foreground service task" }
        Toast.makeText(this, "Demo service is starting", Toast.LENGTH_SHORT).show()

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DemoService::lock").apply {
                    acquire(10 * 60 * 1000L /*10 minutes*/)
                }
            }

        // Background process start from here
        MainService.instance.process()
    }

    private fun stopService() {
        log(Level.INFO, javaClass.simpleName) { "Stopping the foreground service" }
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (e: Exception) {
            log(Level.INFO, javaClass.simpleName) { "Service stopped without being started: ${e.message}" }
        }

        MainService.term()
        isServiceStarted = false
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "DEMO SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            notificationChannelId,
            "Demo Service notifications channel",
            NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.enableLights(true)
            it.lightColor = Color.RED
            it.enableVibration(true)
            it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            it
        }
        notificationManager.createNotificationChannel(channel)

        val pendingIntent: PendingIntent = Intent(this, FullscreenActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)
        }

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, notificationChannelId)

        return builder
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationManager.IMPORTANCE_HIGH) // for under android 26 compatibility
            .build()
    }
}