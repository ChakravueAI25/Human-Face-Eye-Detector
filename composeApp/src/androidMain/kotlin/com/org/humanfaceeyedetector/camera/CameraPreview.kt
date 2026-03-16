package com.org.humanfaceeyedetector.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

private const val TAG = "CameraPreview"

// Global reference to imageCapture for use in capture function
private var currentImageCapture: ImageCapture? = null

/**
 * CameraPreview composable that displays live camera feed using CameraX
 * Supports preview and image capture (Step-5)
 */
@Composable
actual fun CameraPreview(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val isInitialized = remember { mutableStateOf(false) }
    
    // Initialize camera provider and setup
    LaunchedEffect(Unit) {
        initializeCamera(context) { provider ->
            cameraProvider.value = provider
            isInitialized.value = true
        }
    }
    
    if (!isInitialized.value) {
        // Show loading state while camera is being initialized
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        // Render PreviewView once camera is ready
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = modifier.fillMaxSize(),
            update = { previewView ->
                bindCameraToPreview(
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    cameraProvider = cameraProvider.value
                )
            }
        )
    }
}

/**
 * Initialize CameraX ProcessCameraProvider
 */
private fun initializeCamera(
    context: Context,
    onSuccess: (ProcessCameraProvider) -> Unit
) {
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(context)
    
    cameraProviderFuture.addListener(
        {
            try {
                val cameraProvider = cameraProviderFuture.get()
                onSuccess(cameraProvider)
                Log.d(TAG, "Camera provider initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize camera provider", e)
            }
        },
        ContextCompat.getMainExecutor(context)
    )
}

/**
 * Bind camera to PreviewView using CameraX with Preview + ImageCapture (Step-5)
 */
private fun bindCameraToPreview(
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider?
) {
    if (cameraProvider == null) {
        Log.w(TAG, "Camera provider is null, skipping binding")
        return
    }
    
    try {
        // Unbind all previous use cases
        cameraProvider.unbindAll()
        
        // Create preview use case
        val preview = Preview.Builder().build().also { preview ->
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        // Create image capture use case (Step-5)
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        // Store globally for access from capture function
        currentImageCapture = imageCapture
        
        // Select back camera
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        
        // Bind to lifecycle with both preview and image capture (Step-5)
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        
        Log.d(TAG, "Camera and ImageCapture bound to preview successfully")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to bind camera to preview", e)
    }
}

/**
 * Capture image from camera and convert to Bitmap with rotation handling (Step-5)
 * Properly closes ImageProxy to prevent camera freeze
 */
fun captureImage(
    onImageCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit = {}
) {
    val imageCapture = currentImageCapture
    if (imageCapture == null) {
        Log.e(TAG, "ImageCapture not initialized")
        onError("Camera not ready")
        return
    }
    
    val executor = Executors.newSingleThreadExecutor()
    
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    // Get rotation degrees and convert ImageProxy to Bitmap
                    val rotation = image.imageInfo.rotationDegrees
                    val bitmap = imageToBitmap(image)
                    
                    // Apply rotation to bitmap if needed
                    val rotatedBitmap = if (rotation != 0) {
                        rotateBitmap(bitmap, rotation)
                    } else {
                        bitmap
                    }
                    
                    onImageCaptured(rotatedBitmap)
                    Log.d(TAG, "Image captured, converted to Bitmap, and rotated by ${rotation}° successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to convert image to bitmap", e)
                    onError("Failed to process image: ${e.message}")
                } finally {
                    // CRITICAL: Always close ImageProxy to prevent camera freeze
                    image.close()
                }
            }
            
            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Image capture error", exception)
                onError("Capture failed: ${exception.message}")
            }
        }
    )
}

/**
 * Convert ImageProxy to Bitmap
 */
private fun imageToBitmap(imageProxy: ImageProxy): Bitmap {
    val planes = imageProxy.planes
    val ySize = imageProxy.width * imageProxy.height
    val uvSize = imageProxy.width * imageProxy.height / 4
    
    val nv21 = ByteArray(ySize + uvSize * 2)
    
    // Y
    planes[0].buffer.get(nv21, 0, ySize)
    
    val pixelStride = planes[1].pixelStride
    if (pixelStride == 1) {
        // Packed format
        planes[1].buffer.get(nv21, ySize, uvSize)
        planes[2].buffer.get(nv21, ySize + uvSize, uvSize)
    } else {
        // Semi-planar format - interleave U and V
        val uvBuffer = ByteArray(uvSize * 2)
        planes[1].buffer.get(uvBuffer, 0, uvSize)
        planes[2].buffer.get(uvBuffer, uvSize, uvSize)
        
        for (i in 0 until uvSize) {
            nv21[ySize + i * 2] = uvBuffer[i]
            nv21[ySize + i * 2 + 1] = uvBuffer[uvSize + i]
        }
    }
    
    // Convert YUV to RGB
    val bitmap = Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(ySize)
    
    for (i in 0 until ySize) {
        val y = (nv21[i].toInt() and 0xFF) - 16
        val u = (nv21[ySize + (i and -2)].toInt() and 0xFF) - 128
        val v = (nv21[ySize + (i and -2) + 1].toInt() and 0xFF) - 128
        
        val y1192 = if (y < 0) 0 else y * 1192
        var r = y1192 + v * 1634
        var g = y1192 - u * 400 - v * 833
        var b = y1192 + u * 2066
        
        r = if (r > 262143) 262143 else if (r < 0) 0 else r
        g = if (g > 262143) 262143 else if (g < 0) 0 else g
        b = if (b > 262143) 262143 else if (b < 0) 0 else b
        
        pixels[i] = -0x1000000 or (r shl 6 and 0xFF0000) or (g shr 2 and 0xFF00) or (b shr 10 and 0xFF)
    }
    
    bitmap.setPixels(pixels, 0, imageProxy.width, 0, 0, imageProxy.width, imageProxy.height)
    return bitmap
}

/**
 * Rotate bitmap by specified degrees (Step-5 fix)
 * Ensures detection boxes align with actual image orientation
 */
private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return bitmap
    
    val matrix = Matrix().apply {
        postRotate(degrees.toFloat())
    }
    
    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}
