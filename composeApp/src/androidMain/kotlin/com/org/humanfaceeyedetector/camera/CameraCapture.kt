package com.org.humanfaceeyedetector.camera

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
@Suppress("UNUSED")
actual fun RequestCaptureImage(
    onImageCaptured: (ImageBitmap) -> Unit,
    onError: (String) -> Unit
) {
    // Intentionally empty - capture is triggered imperatively via captureImage()
}

actual fun captureImage(
    onImageCaptured: (Any) -> Unit,
    onError: (String) -> Unit
) {
    // Delegate to CameraPreview implementation
    captureImageImpl(
        onImageCaptured = { bitmap ->
            @Suppress("UNCHECKED_CAST")
            onImageCaptured(bitmap as Any)
        },
        onError = onError
    )
}

actual fun Any.toImageBitmap(): ImageBitmap {
    return (this as Bitmap).asImageBitmap()
}

