package com.org.humanfaceeyedetector

import androidx.compose.ui.graphics.ImageBitmap

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getCurrentTimeMillis(): Long

expect fun formatTimestamp(timestamp: Long): String

// Add cropping utility for Step-6

expect fun cropBitmap(image: ImageBitmap, x: Int, y: Int, width: Int, height: Int): ImageBitmap?
