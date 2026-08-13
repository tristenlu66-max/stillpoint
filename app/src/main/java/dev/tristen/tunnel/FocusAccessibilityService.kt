package dev.tristen.tunnel

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class FocusAccessibilityService : AccessibilityService() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var blockerView: android.view.View? = null
    private var blockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            notificationTimeout = 0
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        FocusStore.markAccessibilityEvent(this)
        if (FocusStore.get(this).active) FocusGuardService.start(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return
        FocusStore.markAccessibilityEvent(this)
        val packageName = event.packageName?.toString() ?: return
        val session = FocusStore.get(this)
        if (packageName == FocusStore.SELF) {
            reconcileBlockOverlay()
            return
        }
        if (!session.active || packageName in session.allowed || SystemExemptions.isExempt(this, packageName)) {
            hideBlockOverlay()
            return
        }
        showBlockOverlay(packageName)
    }

    fun showBlockOverlay(packageName: String) = mainHandler.post {
        val session = FocusStore.get(this)
        if (!session.active || packageName in session.allowed || SystemExemptions.isExempt(this, packageName)) return@post
        if (blockerView != null) return@post
        val remaining = ((session.endAt - System.currentTimeMillis()).coerceAtLeast(0) / 60_000L) + 1
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(72, 72, 72, 72)
            setBackgroundColor(Color.rgb(245, 250, 251))
            addView(TextView(context).apply { text = "守一"; textSize = 20f; setTextColor(Color.rgb(42, 156, 191)); gravity = Gravity.CENTER })
            addView(TextView(context).apply { text = "此刻，不去那里。"; textSize = 30f; setTextColor(Color.rgb(23, 55, 71)); gravity = Gravity.CENTER; setPadding(0, 48, 0, 24) })
            addView(TextView(context).apply { text = "你正在「${session.task}」\n还剩约 $remaining 分钟"; textSize = 17f; setTextColor(Color.rgb(42, 156, 191)); gravity = Gravity.CENTER; setPadding(0, 0, 0, 56) })
            addView(Button(context).apply { text = "回到桌面"; setOnClickListener { performGlobalAction(GLOBAL_ACTION_HOME) } })
            addView(Button(context).apply { text = "结束本次专注"; setOnClickListener { FocusStore.stop(this@FocusAccessibilityService); hideBlockOverlay() } })
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            // Keep system focus with the app beneath. The buttons can still be tapped,
            // but the overlay cannot steal the IME or trigger a foreground-app loop.
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        runCatching {
            getSystemService(WindowManager::class.java).addView(panel, params)
            blockerView = panel
            blockedPackage = packageName
        }
    }

    fun hideBlockOverlay() = mainHandler.post {
        blockerView?.let { runCatching { getSystemService(WindowManager::class.java).removeView(it) } }
        blockerView = null
        blockedPackage = null
    }

    fun reconcileBlockOverlay() = mainHandler.post {
        val target = blockedPackage ?: return@post
        val session = FocusStore.get(this)
        if (!session.active || target in session.allowed || SystemExemptions.isExempt(this, target)) {
            hideBlockOverlay()
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        hideBlockOverlay()
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile private var instance: FocusAccessibilityService? = null
        fun showFallbackBlock(packageName: String): Boolean = instance?.let { it.showBlockOverlay(packageName); true } ?: false
        fun hideFallbackBlock() = instance?.hideBlockOverlay()
        fun reconcileFallbackBlock() = instance?.reconcileBlockOverlay()
        fun isActuallyConnected(): Boolean = instance != null
    }
}
