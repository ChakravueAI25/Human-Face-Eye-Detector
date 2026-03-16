package com.org.humanfaceeyedetector.platform

import androidx.compose.runtime.Composable

/**
 * Platform-specific back handler for iOS.
 * iOS doesn't have hardware back button, so this is a no-op.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no hardware back button - navigation handled via SwiftUI navigation stack
}

