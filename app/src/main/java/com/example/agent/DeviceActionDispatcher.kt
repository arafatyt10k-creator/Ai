package com.example.agent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ToolExecutionResult(val success: Boolean, val message: String) {
    class Success(message: String) : ToolExecutionResult(true, message)
    class Failure(message: String) : ToolExecutionResult(false, message)
}

class DeviceActionDispatcher(private val context: Context) {

    private var isTorchOn: Boolean = false

    suspend fun execute(name: String, args: Map<String, String>?): ToolExecutionResult = withContext(Dispatchers.Main) {
        val safeArgs = args ?: emptyMap()
        try {
            when (name) {
                "open_app" -> {
                    val appName = safeArgs["appName"] ?: "app"
                    val pkg = safeArgs["packageName"]
                    openApp(appName, pkg)
                }
                "play_youtube" -> {
                    val query = safeArgs["query"] ?: ""
                    playYoutube(query)
                }
                "make_phone_call" -> {
                    val contact = safeArgs["contactName"]
                    val phone = safeArgs["phoneNumber"]
                    makePhoneCall(contact, phone)
                }
                "send_sms" -> {
                    val contact = safeArgs["contactName"]
                    val phone = safeArgs["phoneNumber"]
                    val msg = safeArgs["message"] ?: ""
                    sendSms(contact, phone, msg)
                }
                "device_toggle" -> {
                    val action = safeArgs["action"] ?: "toggle_flashlight"
                    deviceToggle(action)
                }
                else -> ToolExecutionResult.Failure("Unknown tool: $name")
            }
        } catch (e: Exception) {
            ToolExecutionResult.Failure("Failed to execute $name: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun openApp(appName: String, explicitPackage: String?): ToolExecutionResult {
        val pm = context.packageManager
        val cleanName = appName.trim().lowercase()

        // 1. Direct explicit package
        if (!explicitPackage.isNullOrBlank()) {
            val intent = pm.getLaunchIntentForPackage(explicitPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return ToolExecutionResult.Success("Opened $appName")
            }
        }

        // 2. System setting shortcuts
        if (cleanName.contains("setting") || cleanName.contains("সেটিংস")) {
            val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return ToolExecutionResult.Success("Opened Settings")
        }
        if (cleanName.contains("camera") || cleanName.contains("ক্যামেরা")) {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
                return ToolExecutionResult.Success("Opened Camera")
            }
        }
        if (cleanName.contains("alarm") || cleanName.contains("clock") || cleanName.contains("ঘড়ি")) {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
                return ToolExecutionResult.Success("Opened Clock/Alarm")
            }
        }

        // 3. Known package mapping
        val packageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "ইউটিউব" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "ক্রোম" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "whatsapp" to "com.whatsapp",
            "হোয়াটসঅ্যাপ" to "com.whatsapp",
            "facebook" to "com.facebook.katana",
            "ফেসবুক" to "com.facebook.katana",
            "messenger" to "com.facebook.orca",
            "মেসেঞ্জার" to "com.facebook.orca",
            "instagram" to "com.instagram.android",
            "ইনস্টাগ্রাম" to "com.instagram.android",
            "gmail" to "com.google.android.gm",
            "জিমেইল" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "ম্যাপস" to "com.google.android.apps.maps",
            "spotify" to "com.spotify.music",
            "স্পটিফাই" to "com.spotify.music",
            "play store" to "com.android.vending",
            "প্লে স্টোর" to "com.android.vending",
            "calculator" to "com.google.android.calculator",
            "ক্যালকুলেটর" to "com.google.android.calculator"
        )

        for ((key, pkg) in packageMap) {
            if (cleanName.contains(key)) {
                val intent = pm.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return ToolExecutionResult.Success("Opened $appName")
                }
            }
        }

        // 4. Search installed applications by label
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                val label = pm.getApplicationLabel(app).toString().lowercase()
                if (label.contains(cleanName) || cleanName.contains(label)) {
                    val intent = pm.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        return ToolExecutionResult.Success("Opened $appName")
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        // 5. Fallback web search
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(appName)}"))
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(webIntent)
        return ToolExecutionResult.Success("Searched for $appName")
    }

    fun playYoutube(query: String): ToolExecutionResult {
        val pm = context.packageManager
        val cleanQuery = query.trim()

        try {
            // Try explicit YouTube search intent
            val ytIntent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", cleanQuery)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (ytIntent.resolveActivity(pm) != null) {
                context.startActivity(ytIntent)
                return ToolExecutionResult.Success("Playing '$cleanQuery' on YouTube")
            }

            // Secondary: Launch YouTube view URI
            val viewIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("vnd.youtube.launch://search?q=${Uri.encode(cleanQuery)}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (viewIntent.resolveActivity(pm) != null) {
                context.startActivity(viewIntent)
                return ToolExecutionResult.Success("Playing '$cleanQuery' on YouTube")
            }
        } catch (e: Exception) {
            // fallback to web browser
        }

        // Fallback: standard web browser YouTube
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(cleanQuery)}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(webIntent)
        return ToolExecutionResult.Success("Opening YouTube search for '$cleanQuery'")
    }

    fun makePhoneCall(contactName: String?, rawPhoneNumber: String?): ToolExecutionResult {
        var targetNumber = rawPhoneNumber?.filter { it.isDigit() || it == '+' }

        // If phone number not provided, search contacts
        if (targetNumber.isNullOrBlank() && !contactName.isNullOrBlank()) {
            targetNumber = searchContactPhoneNumber(contactName)
        }

        if (targetNumber.isNullOrBlank()) {
            // Open empty dialer
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
            return ToolExecutionResult.Failure("Could not resolve phone number for ${contactName ?: "contact"}. Opened dialer.")
        }

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$targetNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$targetNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        context.startActivity(intent)
        val nameLabel = contactName ?: targetNumber
        return ToolExecutionResult.Success("Placing call to $nameLabel ($targetNumber)")
    }

    fun sendSms(contactName: String?, rawPhoneNumber: String?, message: String): ToolExecutionResult {
        var targetNumber = rawPhoneNumber?.filter { it.isDigit() || it == '+' }

        if (targetNumber.isNullOrBlank() && !contactName.isNullOrBlank()) {
            targetNumber = searchContactPhoneNumber(contactName)
        }

        if (targetNumber.isNullOrBlank()) {
            // Open SMS composer without recipient
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
            return ToolExecutionResult.Success("Opened SMS composer with your message")
        }

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasSmsPermission) {
            try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(targetNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(targetNumber, null, message, null, null)
                }
                return ToolExecutionResult.Success("Sent SMS to ${contactName ?: targetNumber}: \"$message\"")
            } catch (e: Exception) {
                // fallback to composer
            }
        }

        // Fallback: Open SMS composer
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$targetNumber")
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(smsIntent)
        return ToolExecutionResult.Success("Prepared SMS to ${contactName ?: targetNumber}")
    }

    fun deviceToggle(action: String): ToolExecutionResult {
        return when (action.lowercase()) {
            "toggle_flashlight" -> toggleTorch(!isTorchOn)
            "turn_on_flashlight" -> toggleTorch(true)
            "turn_off_flashlight" -> toggleTorch(false)
            "increase_volume" -> adjustVolume(AudioManager.ADJUST_RAISE)
            "decrease_volume" -> adjustVolume(AudioManager.ADJUST_LOWER)
            "mute_volume" -> adjustVolume(AudioManager.ADJUST_MUTE)
            else -> ToolExecutionResult.Failure("Unknown device toggle action: $action")
        }
    }

    private fun toggleTorch(enable: Boolean): ToolExecutionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull()

            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                isTorchOn = enable
                val stateText = if (enable) "turned on" else "turned off"
                ToolExecutionResult.Success("Flashlight $stateText")
            } else {
                ToolExecutionResult.Failure("No camera flashlight detected on device")
            }
        } catch (e: Exception) {
            ToolExecutionResult.Failure("Flashlight error: ${e.localizedMessage ?: "Hardware unavailable"}")
        }
    }

    private fun adjustVolume(direction: Int): ToolExecutionResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                AudioManager.FLAG_SHOW_UI
            )
            val desc = when (direction) {
                AudioManager.ADJUST_RAISE -> "Media volume increased"
                AudioManager.ADJUST_LOWER -> "Media volume decreased"
                AudioManager.ADJUST_MUTE -> "Media volume muted"
                else -> "Volume adjusted"
            }
            ToolExecutionResult.Success(desc)
        } catch (e: Exception) {
            ToolExecutionResult.Failure("Audio control error: ${e.localizedMessage}")
        }
    }

    private fun searchContactPhoneNumber(contactName: String): String? {
        val hasContactsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasContactsPermission) return null

        var number: String? = null
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$contactName%")

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberIndex != -1) {
                    number = cursor.getString(numberIndex)
                }
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            cursor?.close()
        }

        return number
    }
}
