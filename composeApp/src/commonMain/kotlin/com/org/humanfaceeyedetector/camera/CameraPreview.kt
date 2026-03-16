package com.org.humanfaceeyedetector.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific camera preview composable
 * Android: Uses CameraX to display live camera feed
 * iOS: Placeholder (will use AVCaptureSession)
 */
@Composable
expect fun CameraPreview(modifier: Modifier = Modifier)

