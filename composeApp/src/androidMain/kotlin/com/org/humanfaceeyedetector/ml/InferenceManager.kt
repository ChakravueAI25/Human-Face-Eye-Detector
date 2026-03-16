package com.org.humanfaceeyedetector.ml

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.org.humanfaceeyedetector.state.AppStateHolder
import com.org.humanfaceeyedetector.state.DetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "InferenceManager"

/**
 * Step-6: InferenceManager coordinates ML inference
 * 
 * Responsibilities:
 * - Initialize model
 * - Run detection on background thread
 * - Update app state with results
 * - Handle errors gracefully
 */
object InferenceManager {
    
    /**
     * Initialize ModelRunner with context
     * Must be called at app startup
     */
    fun initialize(context: Context) {
        try {
            ModelRunnerHolder.initialize(context)
            Log.d(TAG, "InferenceManager initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize InferenceManager", e)
        }
    }

    /**
     * Run face detection on captured image
     * Called when user captures an image
     * 
     * @param imageBitmap Captured image from camera
     * @param appState AppState for storing results
     */
    suspend fun detectFaces(
        imageBitmap: ImageBitmap,
        appState: AppStateHolder
    ) {
        withContext(Dispatchers.Default) {
            try {
                appState.setProcessing(true)
                
                // Get ModelRunner instance
                val modelRunner = ModelRunnerHolder.getInstance()
                if (modelRunner == null) {
                    Log.e(TAG, "ModelRunner not initialized")
                    appState.setInferenceError("Model not loaded")
                    return@withContext
                }
                
                // Convert ImageBitmap to Android Bitmap
                val bitmap = imageBitmap.asAndroidBitmap()
                
                // Run detection
                Log.d(TAG, "Running face detection on ${bitmap.width}x${bitmap.height} image")
                val detections: List<Detection> = modelRunner.detect(
                    bitmap = bitmap,
                    originalWidth = bitmap.width,
                    originalHeight = bitmap.height
                )
                
                // Convert to DetectionResult for state storage
                val detectionResults = detections.mapIndexed { index: Int, detection: Detection ->
                    DetectionResult(
                        faceId = index,
                        confidence = detection.confidence,
                        x1 = detection.box.left,
                        y1 = detection.box.top,
                        x2 = detection.box.right,
                        y2 = detection.box.bottom
                    )
                }
                
                // Update state with results
                appState.setDetections(detectionResults)
                Log.d(TAG, "Inference complete: ${detectionResults.size} faces detected")
                
            } catch (e: Exception) {
                Log.e(TAG, "Inference error", e)
                appState.setInferenceError("Detection failed: ${e.message}")
            }
        }
    }
}
