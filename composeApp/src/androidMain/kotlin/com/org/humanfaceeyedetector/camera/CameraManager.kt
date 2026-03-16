package com.org.humanfaceeyedetector.camera

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture

private const val TAG = "CameraManager"

/**
 * Manages camera lifecycle, permissions, and operations
 * Handles:
 * - Camera permission requests
 * - Camera initialization and preview
 * - Future: Image capture and frame analysis
 */
class CameraManager(private val context: Context) {
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var isInitialized = false
    
    /**
     * Start camera preview
     * Must be called after permission is granted
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        onSuccess: (ProcessCameraProvider) -> Unit = {}
    ) {
        if (isInitialized) {
            Log.d(TAG, "Camera already initialized")
            return
        }
        
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener(
            {
                try {
                    val provider = cameraProviderFuture.get()
                    this.cameraProvider = provider
                    isInitialized = true
                    onSuccess(provider)
                    Log.d(TAG, "Camera started successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start camera", e)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }
    
    /**
     * Stop camera and release resources
     */
    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            isInitialized = false
            Log.d(TAG, "Camera stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera", e)
        }
    }
    
    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Capture photo from camera
     * Returns Bitmap to caller for ML inference
     */
    fun capturePhoto(
        onCaptured: (Bitmap) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        try {
            captureImage(onCaptured, onError)
            Log.d(TAG, "Photo capture initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing photo", e)
            onError("Failed to capture photo: ${e.message}")
        }
    }

    // ...existing code...
}

/**
 * Composable helper for camera permission handling and initialization
 */
@Composable
fun rememberCameraManager(context: Context): CameraManager {
    return remember { CameraManager(context) }
}

