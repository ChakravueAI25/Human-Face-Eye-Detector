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
        fullImage: ImageBitmap, // Kept for signature compatibility, unused
        selectedFaceId: Int,
        appState: AppStateHolder
    ) {
        try {
            appState.setProcessing(true)
            
            // Find the selected face by ID
            val selectedFace = appState.state.detections.find { it.faceId == selectedFaceId }
            
            if (selectedFace == null) {
                Log.e(TAG, "Selected face not found (ID: $selectedFaceId)")
                appState.setInferenceError("Face not found")
                appState.setProcessing(false)
                return
            }
            
            Log.d(TAG, "Extracting eyes for face $selectedFaceId")
            
            val faceWidth = abs(selectedFace.x2 - selectedFace.x1)
            
            // Box size: 15% of face width (as per requirements)
            // Using as radius (half-width) for the rect construction
            val eyeBoxRadius = faceWidth * 0.15f
            
            // Get landmarks or fallback
            // Note: coordinates are already mapped to image space in DetectionResult
            
            val leftEyeCenter = getLandmarkOrFallback(
                selectedFace.leftEyeX, selectedFace.leftEyeY,
                selectedFace, isLeftEye = true
            )
            
            val rightEyeCenter = getLandmarkOrFallback(
                selectedFace.rightEyeX, selectedFace.rightEyeY,
                selectedFace, isLeftEye = false
            )
            
            // Create bounding boxes
            val leftEyeBox = createEyeDetection(
                center = leftEyeCenter,
                radius = eyeBoxRadius,
                id = 0 // Left Eye ID
            )
            
            val rightEyeBox = createEyeDetection(
                center = rightEyeCenter,
                radius = eyeBoxRadius,
                id = 1 // Right Eye ID
            )
            
            val eyeDetections = listOf(leftEyeBox, rightEyeBox)
            
            // Update state
            appState.setEyeDetections(eyeDetections)
            appState.setProcessing(false)
            Log.d(TAG, "Eye regions extracted: ${eyeDetections.size}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting eyes", e)
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
