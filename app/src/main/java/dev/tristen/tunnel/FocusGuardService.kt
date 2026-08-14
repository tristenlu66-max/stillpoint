package dev.tristen.tunnel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class FocusGuardService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastKnownForeground: String? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification())
        scope.coroutineContext.cancelChildren()
        scope.launch { while (isActive) { guardOnce(); delay(700) } }
        return START_STICKY
    }
    private fun guardOnce() {
        val session = FocusStore.get(this)
        if (!session.active) { FocusAccessibilityService.hideFallbackBlock(); stopSelf(); return }
        val packageName = foregroundPackage() ?: return
        if (packageName == FocusStore.SELF || packageName in session.allowed || SystemExemptions.isExempt(this, packageName)) {
            FocusAccessibilityService.hideFallbackBlock()
            return
        }
        FocusAccessibilityService.showFallbackBlock(packageName)
    }
    private fun foregroundPackage(): String? {
        val usage = getSystemService(UsageStatsManager::class.java)
        val events = usage.queryEvents(System.currentTimeMillis() - 5_000, System.currentTimeMillis())
        var latest: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) latest = event.packageName
        }
        if (latest != null) lastKnownForeground = latest
        return lastKnownForeground
    }
    private fun notification() : android.app.Notification {
        val channel = NotificationChannel(CHANNEL_ID, "守一专注守护", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_lock_idle_lock).setContentTitle("守一正在守护你的专注").setContentText("专注进行中，点击查看剩余时间").setContentIntent(open).setOngoing(true).build()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { FocusAccessibilityService.hideFallbackBlock(); scope.cancel(); super.onDestroy() }
    companion object {
        private const val CHANNEL_ID="focus_guard"; private const val NOTIFICATION_ID=301
        fun start(context: Context) { runCatching { context.startForegroundService(Intent(context, FocusGuardService::class.java)) } }
    }
}
