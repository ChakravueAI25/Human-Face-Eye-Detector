package com.org.humanfaceeyedetector.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
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
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.org.humanfaceeyedetector.ml.FaceDetector
import com.org.humanfaceeyedetector.state.DetectionResult
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

private const val TAG = "CameraPreview"


// Global reference to imageCapture for use in capture function
private var currentImageCapture: ImageCapture? = null

/**
 * CameraPreview composable that displays live camera feed using CameraX
 * Supports preview and image capture (Step-5)
 */
@Composable
actual fun CameraPreview(
    modifier: Modifier,
    cameraLens: CameraLens,
    isDetectionEnabled: Boolean,
    onDetectionsUpdated: (List<DetectionResult>) -> Unit,
    onImageDimensionsUpdated: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Step-10: Optimize - state for detection enabled
    // We assume detection is enabled by default, but parent can control it logic via updates
    
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
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                }
            },
            modifier = modifier.fillMaxSize(),
            update = { previewView ->
                bindCameraToPreview(
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    cameraProvider = cameraProvider.value,
                    cameraLens = cameraLens,
                    isDetectionEnabled = isDetectionEnabled,
                    onDetectionsUpdated = onDetectionsUpdated,
                    onImageDimensionsUpdated = onImageDimensionsUpdated
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
 * Bind camera to PreviewView using CameraX with Preview + ImageCapture + ImageAnalysis
 */
@OptIn(ExperimentalGetImage::class)
private fun bindCameraToPreview(
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider?,
    cameraLens: CameraLens,
    isDetectionEnabled: Boolean,
    onDetectionsUpdated: (List<DetectionResult>) -> Unit,
    onImageDimensionsUpdated: (Int, Int) -> Unit
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

        // Create image analysis use case
        // Step-10: Backpressure strategy to drop frames if processing is slow
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        
        var lastProcessedTimestamp = 0L
        val throttleIntervalMs = 200L // Process ~5 FPS max

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(previewView.context)) { imageProxy ->
            // Step-10: Skip if detection disabled
            if (!isDetectionEnabled) {
                imageProxy.close()
                return@setAnalyzer
            }
            
            // Step-10: Throttling detection frequency
            val currentTimestamp = System.currentTimeMillis()
            if (currentTimestamp - lastProcessedTimestamp < throttleIntervalMs) {
                imageProxy.close()
                return@setAnalyzer
            }
            
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val rotation = imageProxy.imageInfo.rotationDegrees
                val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
                
                // Update image dimensions
                val width = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
                val height = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height
                onImageDimensionsUpdated(width, height)
                
                // Step-10: Run ML and mapping on background thread (Default dispatcher)
                lifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                    try {
                        lastProcessedTimestamp = System.currentTimeMillis()
                        val faces = FaceDetector.detect(inputImage)
                        
                        // Map to DetectionResult (allocations minimized where possible)
                        val detectionResults = faces.mapIndexed { index, face ->
                            DetectionResult(
                                faceId = face.trackingId ?: index,
                                confidence = 1.0f,
                                x1 = face.boundingBox.left.toFloat(),
                                y1 = face.boundingBox.top.toFloat(),
                                x2 = face.boundingBox.right.toFloat(),
                                y2 = face.boundingBox.bottom.toFloat(),
                                leftEyeX = face.leftEye?.x,
                                leftEyeY = face.leftEye?.y,
                                rightEyeX = face.rightEye?.x,
                                rightEyeY = face.rightEye?.y
                            )
                        }
                        
                        // Switch back to Main for UI update
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                             onDetectionsUpdated(detectionResults)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Analysis error", e)
                    } finally {
                        // Important: close proxy only after processing or copying is done
                        // InputImage.fromMediaImage doesn't copy, it wraps.
                        // However, ML Kit copies internally during process().
                        // We must close imageProxy on the originating thread or after use?
                        // CameraX is strict. imageProxy.close() frees the buffer.
                        // We must trigger close HERE? No, we are in a coroutine.
                        // The analyzer function returns immediately! 
                        // If we return from analyzer, CameraX might think we are done?
                        // If we hold the proxy, capture stalls.
                        
                        // Correction: InputImage from MediaImage holds reference.
                        // We must close imageProxy ONLY when we are done reading from it.
                        // Since 'detect' is suspend and async, we must close it inside the coroutine finally block.
                        // Thread safety: ImageProxy is not thread safe, but we are just reading.
                        imageProxy.close()
                    }
                }
            } else {
                imageProxy.close()
            }
        }

        // Select back/front camera based on lens
        val lensFacing = when(cameraLens) {
            CameraLens.Back -> CameraSelector.LENS_FACING_BACK
            CameraLens.Front -> CameraSelector.LENS_FACING_FRONT
        }
        
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        
        // Bind to lifecycle with both preview, image capture, and image analysis
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
        
        Log.d(TAG, "Camera, ImageCapture, and ImageAnalysis bound to preview successfully with lens: $cameraLens")
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
 * Convert ImageProxy to Bitmap using robust format handling
 */
private fun imageToBitmap(image: ImageProxy): Bitmap {
    val bitmap: Bitmap

    if (image.format == ImageFormat.JPEG) {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } else if (image.format == ImageFormat.YUV_420_888) {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()

        bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } else {
        image.close()
        throw IllegalArgumentException("Unsupported image format: ${image.format}")
    }

    image.close()
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
