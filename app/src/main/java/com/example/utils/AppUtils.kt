package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object AppUtils {
    fun openApp(context: Context, appNameOrPackage: String): Boolean {
        val pm = context.packageManager
        
        // Map common app names to packages
        val packageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "facebook" to "com.facebook.katana",
            "messenger" to "com.facebook.orca",
            "whatsapp" to "com.whatsapp",
            "instagram" to "com.instagram.android",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "play store" to "com.android.vending",
            "tiktok" to "com.zhiliaoapp.musically",
            "spotify" to "com.spotify.music"
        )
        
        val targetPackage = packageMap[appNameOrPackage.lowercase()] ?: appNameOrPackage
        
        return try {
            val intent = pm.getLaunchIntentForPackage(targetPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                // If not found, try searching in Play Store or using a web intent
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$appNameOrPackage"))
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
