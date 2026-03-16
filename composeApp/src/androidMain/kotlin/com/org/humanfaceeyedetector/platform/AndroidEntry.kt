package com.org.humanfaceeyedetector.platform

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Platform-specific back handler for Android.
 * Wraps BackHandler to intercept Android back button.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
