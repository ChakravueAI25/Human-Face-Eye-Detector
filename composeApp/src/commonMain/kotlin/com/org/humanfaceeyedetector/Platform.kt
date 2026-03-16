package com.org.humanfaceeyedetector

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform