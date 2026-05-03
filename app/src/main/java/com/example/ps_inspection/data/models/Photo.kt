package com.example.ps_inspection.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Photo(
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return format.format(Date(timestamp))
    }

    fun getFormattedTimeShort(): String {
        val format = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}