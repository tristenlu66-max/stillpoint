package dev.tristen.tunnel

import android.content.Context
import android.content.Intent

object FocusBlocker {
    @Volatile private var lastLaunchAt = 0L
    @Volatile private var lastPackage = ""
    fun block(context: Context, packageName: String) {
        val now = System.currentTimeMillis()
        if (packageName == lastPackage && now - lastLaunchAt < 450L) return
        lastPackage = packageName; lastLaunchAt = now
        context.startActivity(Intent(context, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, packageName)
        })
    }
}
