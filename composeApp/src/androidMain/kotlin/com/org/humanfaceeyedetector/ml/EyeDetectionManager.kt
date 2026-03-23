package com.org.humanfaceeyedetector.ml

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import com.org.humanfaceeyedetector.state.AppStateHolder
import com.org.humanfaceeyedetector.state.DetectionResult
import kotlin.math.abs

private const val TAG = "EyeDetectionManager"

/**
 * Step-8: EyeDetectionManager extracts eye regions from existing landmarks
 * 
 * Replaces previous ML-based approach with geometric extraction.
 * Constraints:
 * - Do not run additional ML models
 * - Use existing landmarks from selected face
 */
object EyeDetectionManager {
    
    /**
     * Extract eye boxes from selected face
     * Using existing landmarks, no new inference.
     */
    suspend fun detectEyes(
        fullImage: ImageBitmap,
        selectedFaceId: Int,
        appState: AppStateHolder
    ) {
        try {
            appState.setProcessing(true)

            val selectedFace = appState.state.detections.getOrNull(selectedFaceId)
            if (selectedFace == null) {
                appState.setInferenceError("Face not found")
                appState.setProcessing(false)
                return
            }

            val faceWidth  = abs(selectedFace.x2 - selectedFace.x1)
            val faceHeight = abs(selectedFace.y2 - selectedFace.y1)
            val eyeBoxHalfW = faceWidth  * 0.20f
            val eyeBoxHalfH = faceHeight * 0.12f

            val leftCenter = getLandmarkOrFallback(
                selectedFace.leftEyeX, selectedFace.leftEyeY, selectedFace, isLeftEye = true
            )
            val rightCenter = getLandmarkOrFallback(
                selectedFace.rightEyeX, selectedFace.rightEyeY, selectedFace, isLeftEye = false
            )

            fun makeBox(center: Pair<Float,Float>, id: Int) = DetectionResult(
                faceId     = id,
                confidence = 1.0f,
                x1 = center.first  - eyeBoxHalfW,
                y1 = center.second - eyeBoxHalfH,
                x2 = center.first  + eyeBoxHalfW,
                y2 = center.second + eyeBoxHalfH
            )

            appState.setEyeDetections(listOf(makeBox(leftCenter, 0), makeBox(rightCenter, 1)))
            appState.setProcessing(false)

        } catch (e: Exception) {
            appState.setInferenceError("Eye extraction failed: ${e.message}")
            appState.setProcessing(false)
        }
    }
    
    private fun getLandmarkOrFallback(
        x: Float?, 
        y: Float?, 
        face: DetectionResult,
        isLeftEye: Boolean
    ): Pair<Float, Float> {
        if (x != null && y != null) {
            return Pair(x, y)
        }
        
        Log.w(TAG, "Missing landmark for ${if(isLeftEye) "Left" else "Right"} eye. Using fallback.")
        
        val width = abs(face.x2 - face.x1)
        val height = abs(face.y2 - face.y1)
        
        // Fallback estimation relative to face box
        // ML Kit: LEFT_EYE is person's left (viewer's right)
        // Viewer's Right (Person's Left Eye) -> xRatio ~ 0.7
        // Viewer's Left (Person's Right Eye) -> xRatio ~ 0.3
        
        val xRatio = if (isLeftEye) 0.7f else 0.3f
        val yRatio = 0.35f 
        
        val estimatedX = face.x1 + (width * xRatio)
        val estimatedY = face.y1 + (height * yRatio)
        
        return Pair(estimatedX, estimatedY)
    }
    
    private fun createEyeDetection(
        center: Pair<Float, Float>,
        radius: Float,
        id: Int
    ): DetectionResult {
        val (cx, cy) = center
        
        return DetectionResult(
            faceId = id,
            confidence = 1.0f,
            x1 = cx - radius,
            y1 = cy - radius,
            x2 = cx + radius,
            y2 = cy + radius
        )
    }
}
