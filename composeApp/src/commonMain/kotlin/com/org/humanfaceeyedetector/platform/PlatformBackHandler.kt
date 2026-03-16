package com.org.humanfaceeyedetector.platform

import androidx.compose.runtime.Composable

/**
 * Platform-specific back handler.
 * Android: Intercepts hardware back button
 * iOS: No-op (iOS handles navigation via SwiftUI)
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

