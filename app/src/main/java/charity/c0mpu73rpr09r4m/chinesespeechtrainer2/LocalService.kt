package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class LocalService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    private val localBinder = LocalBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = getNotification(null)
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    fun getNotification(input: String?): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)

        if (!input.isNullOrBlank()) {
            builder.setContentText(input)
        }

        return builder
            .setSmallIcon(R.drawable.ic_stat_name)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    fun showNotification(context: Context, input: String?) {
        val notification = getNotification(input)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder {
        return localBinder
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocalService {
            // Return this instance of LocalService so clients can call public methods
            return this@LocalService
        }
    }
}