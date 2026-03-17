package com.org.humanfaceeyedetector.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.org.humanfaceeyedetector.state.DetectionResult

enum class CameraLens {
    Front, Back
}

/**
 * Platform-specific camera preview composable
 * Android: Uses CameraX to display live camera feed
 * iOS: Placeholder (will use AVCaptureSession)
 */
@Composable
expect fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraLens: CameraLens = CameraLens.Back,
    isDetectionEnabled: Boolean = true,
    onDetectionsUpdated: (List<DetectionResult>) -> Unit = {},
    onImageDimensionsUpdated: (Int, Int) -> Unit = { _, _ -> }
)
