package dev.tristen.tunnel

import android.content.Context
import android.content.Intent

object SystemExemptions {
    fun isExempt(context: Context, packageName: String): Boolean = packageName in fixed || packageName in homePackages(context)
    private fun homePackages(context: Context): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager.queryIntentActivities(homeIntent, 0).map { it.activityInfo.packageName }.toSet()
    }
    private val fixed = setOf(
        "android", "com.android.systemui", "com.android.settings", "com.vivo.settings",
        "com.android.permissioncontroller", "com.google.android.permissioncontroller", "com.vivo.permissionmanager",
        "com.android.inputmethod.latin", "com.google.android.inputmethod.latin", "com.iflytek.inputmethod",
        "com.android.documentsui", "com.android.filemanager", "com.android.camera", "com.android.dialer",
        "com.google.android.dialer", "com.android.packageinstaller", "com.google.android.packageinstaller"
    )
}
