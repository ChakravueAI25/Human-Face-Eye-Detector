package com.org.humanfaceeyedetector.camera

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.Composable

/**
 * Platform-specific camera capture function
 * Android: Uses CameraX ImageCapture
 * iOS: Will use AVCaptureSession
 */
@Composable
expect fun RequestCaptureImage(
    onImageCaptured: (ImageBitmap) -> Unit,
    onError: (String) -> Unit = {}
)

