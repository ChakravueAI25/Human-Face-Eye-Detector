package com.org.humanfaceeyedetector.ml

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.mlkit.vision.common.InputImage
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
                
                // Convert ImageBitmap to Android Bitmap
                val bitmap = imageBitmap.asAndroidBitmap()
                
                // TEST FUNCTION (MANDATORY)
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val faces = FaceDetector.detect(inputImage)

                Log.d("TEST", "Faces: ${faces.size}")

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
                
                // Update state with results
                appState.setDetections(detectionResults)
                appState.setProcessing(false)
                

            } catch (e: Exception) {
                Log.e(TAG, "Inference error", e)
                appState.setInferenceError("Detection failed: ${e.message}")
            }
        }
    }
}
