package com.narik.mania.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.narik.mania.R
import com.narik.mania.data.JitterStateHolder
import com.narik.mania.domain.GeoPoint
import com.narik.mania.domain.JitterEngine
import com.narik.mania.domain.JitterMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class JitterService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val mode = intent.getStringExtra(EXTRA_MODE)
                    ?.let { JitterMode.valueOf(it) } ?: JitterMode.GRB
                val center = GeoPoint(
                    intent.getDoubleExtra(EXTRA_LAT, 0.0),
                    intent.getDoubleExtra(EXTRA_LNG, 0.0)
                )
                startForeground(NOTIF_ID, buildNotification(mode), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                JitterStateHolder.startRun(mode, center)
                job?.cancel()
                job = scope.launch { runLoop(mode, center) }
            }
            ACTION_STOP -> stopEverything()
        }
        return START_NOT_STICKY
    }

    private suspend fun runLoop(mode: JitterMode, center: GeoPoint) {
        while (currentCoroutineContext().isActive) {
            val s = JitterStateHolder.state.value
            if (!s.isRunning) break
            val next = when (mode) {
                JitterMode.GRB -> JitterEngine.nextGrb(s.point, center, s.step.toDouble(), s.radius.toDouble())
                JitterMode.GJK -> JitterEngine.nextGjk(s.point, center, s.step.toDouble(), s.radius.toDouble())
            }
            JitterStateHolder.update { it.copy(point = next) }
            notificationManager.notify(NOTIF_ID, buildNotification(mode))
            delay(s.interval * 1000L)
        }
    }

    private fun stopEverything() {
        job?.cancel()
        job = null
        JitterStateHolder.stopRun()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(mode: JitterMode): Notification {
        val s = JitterStateHolder.state.value
        val text = String.format(
            Locale.US,
            "Langkah %.1f m • Radius %.1f m • Interval %d dtk",
            s.step, s.radius, s.interval
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_narik)
            .setContentTitle("NARIK • $mode AKTIF")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "narik_jitter_channel"
        const val NOTIF_ID = 1001
        private const val ACTION_START = "com.narik.mania.action.START"
        private const val ACTION_STOP = "com.narik.mania.action.STOP"
        private const val EXTRA_MODE = "extra_mode"
        private const val EXTRA_LAT = "extra_lat"
        private const val EXTRA_LNG = "extra_lng"

        fun startIntent(context: Context, mode: JitterMode, center: GeoPoint): Intent =
            Intent(context, JitterService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MODE, mode.name)
                putExtra(EXTRA_LAT, center.lat)
                putExtra(EXTRA_LNG, center.lng)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, JitterService::class.java).apply { action = ACTION_STOP }
    }
}
