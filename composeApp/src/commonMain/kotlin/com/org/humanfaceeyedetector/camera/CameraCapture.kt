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

/**
 * Imperatively trigger image capture
 * Android: Uses CameraX ImageCapture.takePicture
 * iOS: Will use AVCapturePhotoOutput
 */
expect fun captureImage(
    onImageCaptured: (Any) -> Unit,
    onError: (String) -> Unit = {}
)

/**
 * Extension to convert platform Bitmap to Compose ImageBitmap
 * Android: Bitmap.asImageBitmap()
 */
expect fun Any.toImageBitmap(): ImageBitmap

