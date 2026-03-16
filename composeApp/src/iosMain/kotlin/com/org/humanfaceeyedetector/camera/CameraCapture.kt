package com.org.humanfaceeyedetector.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.sp

/**
 * iOS stub for camera capture
 * Will use AVCaptureSession in future implementation
 */
@Composable
actual fun RequestCaptureImage(
    onImageCaptured: (ImageBitmap) -> Unit,
    onError: (String) -> Unit
) {
    onError("Camera capture not yet implemented for iOS")
}

