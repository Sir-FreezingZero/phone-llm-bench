package dev.phonelab.bench

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.PowerManager
import kotlin.concurrent.thread

/** Foreground service so a test round keeps running with the screen off. */
class BenchService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Speed tests", NotificationManager.IMPORTANCE_LOW)
        )
        startForeground(ID, note("Starting…", true), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        if (Lab.running) return START_NOT_STICKY

        val wl = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "llmlab:bench")
        wl.acquire(6 * 60 * 60 * 1000L)
        Lab.running = true
        Lab.stopRequested = false
        Lab.onStatus = { s -> nm.notify(ID, note(s, true)) }

        thread(name = "bench") {
            var final = "Done"
            try {
                Runner(this).run()
                final = Lab.status
            } catch (e: Throwable) {
                final = "Failed: ${e.message}"
                Lab.setStatus(final)
            } finally {
                Lab.onStatus = null
                Lab.running = false
                if (wl.isHeld) wl.release()
                stopForeground(STOP_FOREGROUND_REMOVE)
                nm.notify(ID + 1, note(final, false))
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun note(text: String, ongoing: Boolean): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("LLM Speed Lab")
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setContentIntent(open)
            .build()
    }

    companion object {
        const val CHANNEL = "bench"
        const val ID = 42

        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, BenchService::class.java))
        }
    }
}
