package com.org.humanfaceeyedetector.ml

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.org.humanfaceeyedetector.state.AppStateHolder
import com.org.humanfaceeyedetector.state.DetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "EyeDetectionManager"

/**
 * Step-7: EyeDetectionManager coordinates eye detection within face region
 */
object EyeDetectionManager {
    
    /**
     * Detect eyes within selected face
     * 
     * @param fullImage Original captured image
     * @param selectedFaceIndex Which face was selected
     * @param appState App state containing face detections
     */
    suspend fun detectEyes(
        fullImage: ImageBitmap,
        selectedFaceIndex: Int,
        appState: AppStateHolder
    ) {
        withContext(Dispatchers.Default) {
            try {
                appState.setProcessing(true)
                
                // Get the selected face detection
                val selectedFace = appState.state.detections.getOrNull(selectedFaceIndex)
                if (selectedFace == null) {
                    Log.e(TAG, "Selected face not found")
                    appState.setInferenceError("Face not found")
                    return@withContext
                }
                
                // Get ModelRunner instance
                val modelRunner = ModelRunnerHolder.getInstance()
                if (modelRunner == null) {
                    Log.e(TAG, "ModelRunner not initialized")
                    appState.setInferenceError("Model not loaded")
                    return@withContext
                }
                
                // Convert ImageBitmap to Bitmap
                val bitmap = fullImage.asAndroidBitmap()
                
                // Step 1: Crop face region
                val croppedFace = FaceCropper.cropFace(
                    bitmap = bitmap,
                    left = selectedFace.x1,
                    top = selectedFace.y1,
                    right = selectedFace.x2,
                    bottom = selectedFace.y2
                )
                
                // Step 2: Run inference on cropped face
                Log.d(TAG, "Running eye detection on cropped face: ${croppedFace.width}x${croppedFace.height}")
                modelRunner.detect(
                    bitmap = croppedFace,
                    originalWidth = croppedFace.width,
                    originalHeight = croppedFace.height
                )
                
                // Step 3: Filter for eye-like detections
                val eyeDetectionsFiltered = DetectionParser.parseEyes(
                    output = modelRunner.getLastOutput() ?: return@withContext,
                    faceWidth = croppedFace.width,
                    faceHeight = croppedFace.height
                )
                
                if (eyeDetectionsFiltered.isEmpty()) {
                    Log.w(TAG, "No eyes detected")
                    appState.setEyeDetections(emptyList())
                    appState.setProcessing(false)
                    return@withContext
                }
                
                // Step 4: Map coordinates back to full image
                val eyeResultsInFullImage = eyeDetectionsFiltered.mapIndexed { index, detection ->
                    DetectionResult(
                        faceId = index,  // 0=left eye, 1=right eye
                        confidence = detection.confidence,
                        x1 = selectedFace.x1 + detection.box.left,
                        y1 = selectedFace.y1 + detection.box.top,
                        x2 = selectedFace.x1 + detection.box.right,
                        y2 = selectedFace.y1 + detection.box.bottom
                    )
                }
                
                // Step 5: Store in state
                appState.setEyeDetections(eyeResultsInFullImage)
                Log.d(TAG, "Eye detection complete: ${eyeResultsInFullImage.size} eyes found")
                
            } catch (e: Exception) {
                Log.e(TAG, "Eye detection error", e)
                appState.setInferenceError("Eye detection failed: ${e.message}")
            }
        }
    }
}

