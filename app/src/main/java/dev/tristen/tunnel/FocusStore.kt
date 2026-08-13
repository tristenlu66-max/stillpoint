package dev.tristen.tunnel

import android.content.Context
import android.content.Intent

data class FocusSession(val active: Boolean, val task: String, val endAt: Long, val allowed: Set<String>)

object FocusStore {
    private const val PREFS = "focus_prefs"
    private const val KEY_ACTIVE = "active"
    private const val KEY_TASK = "task"
    private const val KEY_END = "end"
    private const val KEY_ALLOWED = "allowed"
    const val SELF = "dev.tristen.tunnel"
    private const val KEY_LAST_EVENT = "last_accessibility_event"

    fun get(context: Context): FocusSession {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val active = p.getBoolean(KEY_ACTIVE, false) && p.getLong(KEY_END, 0) > System.currentTimeMillis()
        if (!active && p.getBoolean(KEY_ACTIVE, false)) p.edit().putBoolean(KEY_ACTIVE, false).apply()
        return FocusSession(active, p.getString(KEY_TASK, "专注") ?: "专注", p.getLong(KEY_END, 0), p.getStringSet(KEY_ALLOWED, emptySet()) ?: emptySet())
    }

    fun start(context: Context, task: String, minutes: Int, allowed: Set<String>) {
        // A previous session may have left an overlay on screen while its app was
        // later added to the whitelist. Never carry that visual state forward.
        FocusAccessibilityService.hideFallbackBlock()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ACTIVE, true).putString(KEY_TASK, task.ifBlank { "专注" })
            .putLong(KEY_END, System.currentTimeMillis() + minutes * 60_000L)
            .putStringSet(KEY_ALLOWED, allowed).apply()
        FocusGuardService.start(context)
    }
    fun updateAllowed(context: Context, allowed: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY_ALLOWED, allowed).apply()
        FocusAccessibilityService.reconcileFallbackBlock()
    }
    fun markAccessibilityEvent(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(KEY_LAST_EVENT, System.currentTimeMillis()).apply()
    fun lastAccessibilityEvent(context: Context): Long = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_LAST_EVENT, 0)
    fun stop(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ACTIVE, false).apply()
        FocusAccessibilityService.hideFallbackBlock()
        context.stopService(Intent(context, FocusGuardService::class.java))
    }
}
