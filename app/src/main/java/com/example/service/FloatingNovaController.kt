package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.utils.OverlayPermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FloatingNovaController {

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    internal fun setOverlayRunning(running: Boolean) {
        _isOverlayActive.value = running
    }

    /**
     * Starts the Floating NOVA Jarvis Screen Overlay.
     * Returns true if started, or false if overlay permission is required.
     */
    fun start(context: Context): Boolean {
        if (!OverlayPermissionManager.canDrawOverlays(context)) {
            OverlayPermissionManager.requestOverlayPermission(context)
            return false
        }

        val intent = Intent(context, FloatingNovaService::class.java).apply {
            action = FloatingNovaService.ACTION_START
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Stops the Floating NOVA Screen Overlay.
     */
    fun stop(context: Context) {
        val intent = Intent(context, FloatingNovaService::class.java).apply {
            action = FloatingNovaService.ACTION_STOP
        }
        try {
            context.stopService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isOverlayActive.value = false
    }

    /**
     * Toggles overlay on or off.
     */
    fun toggle(context: Context): Boolean {
        return if (_isOverlayActive.value || isServiceRunning(context)) {
            stop(context)
            false
        } else {
            start(context)
        }
    }

    /**
     * Fallback check if the service is currently running in the OS.
     */
    fun isServiceRunning(context: Context): Boolean {
        if (_isOverlayActive.value) return true
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (FloatingNovaService::class.java.name == service.service.className) {
                _isOverlayActive.value = true
                return true
            }
        }
        return false
    }
}
