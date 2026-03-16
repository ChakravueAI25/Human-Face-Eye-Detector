package com.org.humanfaceeyedetector

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun cropBitmap(image: ImageBitmap, x: Int, y: Int, width: Int, height: Int): ImageBitmap? {
    return try {
        val bitmap = image.asAndroidBitmap()

        // Ensure bounds are valid
        val safeX = x.coerceAtLeast(0)
        val safeY = y.coerceAtLeast(0)

        // If x is outside, width becomes 0 or negative if we don't handle it
        if (safeX >= bitmap.width || safeY >= bitmap.height) return null

        val safeWidth = width.coerceAtMost(bitmap.width - safeX)
        val safeHeight = height.coerceAtMost(bitmap.height - safeY)

        if (safeWidth <= 0 || safeHeight <= 0) return null

        val cropped = Bitmap.createBitmap(bitmap, safeX, safeY, safeWidth, safeHeight)
        cropped.asImageBitmap()
    } catch (e: Exception) {
        Log.e("Platform.android", "Failed to crop bitmap", e)
        null
    }
}

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
