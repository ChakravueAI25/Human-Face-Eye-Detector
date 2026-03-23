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

                val bitmapW = bitmap.width.toFloat()
                val bitmapH = bitmap.height.toFloat()

                val detectionResults = faces.mapIndexed { index, face ->
                    val rawLeftX  = face.leftEye?.x
                    val rawLeftY  = face.leftEye?.y
                    val rawRightX = face.rightEye?.x
                    val rawRightY = face.rightEye?.y

                    fun Float?.clampW() = this?.coerceIn(0f, bitmapW)
                    fun Float?.clampH() = this?.coerceIn(0f, bitmapH)

                    DetectionResult(
                        faceId     = face.trackingId ?: index,
                        confidence = 1.0f,
                        x1 = face.boundingBox.left.toFloat().coerceIn(0f, bitmapW),
                        y1 = face.boundingBox.top.toFloat().coerceIn(0f, bitmapH),
                        x2 = face.boundingBox.right.toFloat().coerceIn(0f, bitmapW),
                        y2 = face.boundingBox.bottom.toFloat().coerceIn(0f, bitmapH),
                        leftEyeX  = rawLeftX.clampW(),
                        leftEyeY  = rawLeftY.clampH(),
                        rightEyeX = rawRightX.clampW(),
                        rightEyeY = rawRightY.clampH()
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
