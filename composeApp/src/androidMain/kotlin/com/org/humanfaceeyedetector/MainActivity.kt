package com.org.humanfaceeyedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.org.humanfaceeyedetector.permissions.CameraPermissionHelper
import com.org.humanfaceeyedetector.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Configure status bar and navigation bar appearance for dark theme
        // Light icons on dark background
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController?.let {
            it.isAppearanceLightStatusBars = false
            it.isAppearanceLightNavigationBars = false
        }

        // Request camera permission (Android 6+)
        CameraPermissionHelper.checkAndRequestPermission(this)

        setContent {
            App()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CameraPermissionHelper.handlePermissionResult(
            requestCode,
            permissions,
            grantResults,
            onGranted = { },
            onDenied = { }
        )
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}