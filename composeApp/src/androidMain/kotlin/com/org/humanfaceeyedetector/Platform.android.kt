package com.org.humanfaceeyedetector

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

actual fun formatTimestamp(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        java.util.Locale.getDefault()
    )
    return formatter.format(java.util.Date(timestamp))
}
