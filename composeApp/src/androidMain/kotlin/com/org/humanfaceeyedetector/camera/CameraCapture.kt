package com.org.humanfaceeyedetector.camera

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Android implementation of camera capture
 * Uses the global captureImage function from CameraPreview
 * Step-5: Includes ImageCapture with rotation handling
 */
@Composable
actual fun RequestCaptureImage(
    onImageCaptured: (ImageBitmap) -> Unit,
    onError: (String) -> Unit
) {
    captureImage(
        onImageCaptured = { bitmap ->
            val imageBitmap = bitmap.asImageBitmap()
            onImageCaptured(imageBitmap)
        },
        onError = onError
    )
}

