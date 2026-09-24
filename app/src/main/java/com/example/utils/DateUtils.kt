package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatTimestamp(millis: Long, isBengali: Boolean = true): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(Date(millis))

        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1

        val datePrefix = when {
            isToday -> if (isBengali) "আজ" else "Today"
            isYesterday -> if (isBengali) "গতকাল" else "Yesterday"
            else -> {
                val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                dateFormat.format(Date(millis))
            }
        }

        return "$datePrefix • $timeStr"
    }

    fun formatDueDate(millis: Long?, isBengali: Boolean = true): String {
        if (millis == null) return if (isBengali) "তারিখ নেই" else "No due date"
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }

        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        val isTomorrow = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                target.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR) == 1

        return when {
            isToday -> if (isBengali) "আজকের মধ্যে" else "Today"
            isTomorrow -> if (isBengali) "আগামীকাল" else "Tomorrow"
            else -> {
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                dateFormat.format(Date(millis))
            }
        }
    }

    fun getTimeBasedGreeting(userName: String = "", isBengali: Boolean = true): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (isBengali) {
            val greeting = when (hour) {
                in 5..11 -> "শুভ সকাল"
                in 12..16 -> "শুভ দুপুর"
                in 17..19 -> "শুভ বিকেল"
                else -> "শুভ রাত্রি"
            }
            if (userName.isNotBlank() && userName != "User" && userName != "Explorer") {
                "$greeting, $userName!"
            } else {
                "$greeting!"
            }
        } else {
            val greeting = when (hour) {
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..19 -> "Good evening"
                else -> "Good night"
            }
            if (userName.isNotBlank() && userName != "User" && userName != "Explorer") {
                "$greeting, $userName!"
            } else {
                "$greeting!"
            }
        }
    }
}
