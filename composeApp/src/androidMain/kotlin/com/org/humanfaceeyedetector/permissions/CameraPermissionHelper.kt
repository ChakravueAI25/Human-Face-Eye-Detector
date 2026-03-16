package com.org.humanfaceeyedetector.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Helper for runtime camera permissions (Android 6+)
 * Required because <uses-permission> in manifest is not enough on modern Android
 * 
 * Integration in MainActivity:
 * 1. In onCreate(), call: CameraPermissionHelper.checkAndRequestPermission(this)
 * 2. Override onRequestPermissionsResult() to handle result
 */
object CameraPermissionHelper {
    
    private const val CAMERA_PERMISSION_CODE = 100
    
    /**
     * Check if camera permission is already granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check and request camera permission if needed
     * Call this in Activity.onCreate()
     */
    fun checkAndRequestPermission(activity: android.app.Activity) {
        if (!hasCameraPermission(activity)) {
            // Request permission
            activity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }
    
    /**
     * Handle permission result
     * Call this from Activity.onRequestPermissionsResult()
     * 
     * @param requestCode Should match CAMERA_PERMISSION_CODE
     * @param onGranted Callback when permission is granted
     * @param onDenied Callback when permission is denied
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}


