package dev.tristen.tunnel

import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager

object SystemExemptions {
    fun isExempt(context: Context, packageName: String): Boolean =
        packageName in fixed || packageName in homePackages(context) || packageName in inputMethodPackages(context) ||
            isSystemIntermediatePackage(context, packageName)

    private fun homePackages(context: Context): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager.queryIntentActivities(homeIntent, 0).map { it.activityInfo.packageName }.toSet()
    }

    /**
     * Keyboard windows are reported as ordinary foreground windows on some vivo
     * builds. The public InputMethodManager API gives us enabled keyboards without
     * reading Settings.Secure keys, which Android 14+ forbids for targetSdk 34+.
     */
    private fun inputMethodPackages(context: Context): Set<String> = buildSet {
        runCatching {
            context.getSystemService(InputMethodManager::class.java)
                .enabledInputMethodList
                .mapTo(this) { it.packageName }
        }
    }

    /**
     * Pickers and permission hand-offs are often exported Android components with
     * no launcher icon. They are never destinations the user can normally open,
     * so treating them as a focus target only creates a flickering block overlay.
     */
    private fun isSystemIntermediatePackage(context: Context, packageName: String): Boolean =
        packageName.startsWith("com.android.") && context.packageManager.getLaunchIntentForPackage(packageName) == null

    private val fixed = setOf(
        "android", "com.android.systemui", "com.android.settings", "com.vivo.settings",
        "com.android.permissioncontroller", "com.google.android.permissioncontroller", "com.vivo.permissionmanager",
        "com.android.inputmethod.latin", "com.google.android.inputmethod.latin", "com.iflytek.inputmethod",
        "com.baidu.input_vivo", "com.vivo.ai.ime.nex", "com.vivo.secime.service",
        "com.android.documentsui", "com.android.filemanager", "com.android.camera", "com.android.dialer",
        "com.google.android.dialer", "com.android.packageinstaller", "com.google.android.packageinstaller",
        // Android's picker is a separate system UI, not the user's Gallery app.
        // Chrome and ChatGPT use it for attachments, even when Gallery is allowed.
        "com.android.photopicker", "com.android.providers.media", "com.android.providers.media.module",
        "com.android.providers.downloads", "com.android.providers.downloads.ui", "com.android.externalstorage",
        "com.vivo.gallery", "com.vivo.base.gallery", "com.vivo.alldocuments",
        // vivo's built-in screen recording controls and their system overlay hosts.
        "com.vivo.smartshot", "com.vivo.screenagent", "com.vivo.upslide", "com.vivo.floatingball", "com.vivo.systemuiplugin"
    )
}
