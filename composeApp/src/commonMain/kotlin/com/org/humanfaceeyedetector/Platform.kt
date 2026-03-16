package com.org.humanfaceeyedetector

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getCurrentTimeMillis(): Long

expect fun formatTimestamp(timestamp: Long): String
