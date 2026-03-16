package com.org.humanfaceeyedetector.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

/**
 * iOS stub for CameraPreview
 * Uses AVCaptureSession via SwiftUI integration
 * Full implementation will be added in iOS Step-4
 */
@Composable
actual fun CameraPreview(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Camera Preview\n(iOS - Coming Soon)",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

