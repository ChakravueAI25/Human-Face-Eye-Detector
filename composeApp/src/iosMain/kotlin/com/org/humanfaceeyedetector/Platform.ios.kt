package com.org.humanfaceeyedetector

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import androidx.compose.ui.graphics.ImageBitmap

actual fun cropBitmap(image: ImageBitmap, x: Int, y: Int, width: Int, height: Int): ImageBitmap? {
    // Basic implementation skipped for iOS pending Step-4
    return null
}

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun formatTimestamp(timestamp: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm"
    return formatter.stringFromDate(date)
}
