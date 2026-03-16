package com.org.humanfaceeyedetector.ml

import android.graphics.RectF
import android.util.Log

private const val TAG = "DetectionParser"
private const val CONFIDENCE_THRESHOLD = 0.45f
private const val EYE_CONFIDENCE_THRESHOLD = 0.35f

/**
 * Step-6: Detection result from ML model
 * Represents a detected face with bounding box and confidence score
 */
data class Detection(
    val box: RectF,           // Bounding box in image coordinates
    val confidence: Float,    // Confidence score [0, 1]
    val classId: Int = 0      // Class ID (0 = face)
)

/**
 * Step-6/7: Parses TensorFlow Lite model output
 * 
 * Input: Raw model output [1, 300, 6]
 * - x1, y1: Top-left corner
 * - x2, y2: Bottom-right corner
 * - confidence: Detection confidence
 * - classId: Object class
 * 
 * Output: List of Detection objects filtered by confidence threshold
 */
object DetectionParser {
    
    /**
     * Parse model output into Detection objects (faces)
     * 
     * @param output Raw model output array [1, 300, 6]
     * @param imageWidth Original image width (for coordinate scaling)
     * @param imageHeight Original image height (for coordinate scaling)
     * @return Filtered list of detections above confidence threshold
     */
    fun parse(
        output: Array<Array<FloatArray>>,
        imageWidth: Int,
        imageHeight: Int
    ): List<Detection> {
        val detections = mutableListOf<Detection>()
        
        try {
            // Iterate through all 300 possible detections
            for (i in 0 until 300) {
                val x1 = output[0][i][0]
                val y1 = output[0][i][1]
                val x2 = output[0][i][2]
                val y2 = output[0][i][3]
                val confidence = output[0][i][4]
                val classId = output[0][i][5].toInt()
                
                // Filter by confidence threshold
                if (confidence > CONFIDENCE_THRESHOLD) {
                    // Scale coordinates from model space (640x640) to image space
                    val scaleX = imageWidth.toFloat() / 640f
                    val scaleY = imageHeight.toFloat() / 640f
                    
                    val scaledX1 = x1 * scaleX
                    val scaledY1 = y1 * scaleY
                    val scaledX2 = x2 * scaleX
                    val scaledY2 = y2 * scaleY
                    
                    // Create detection with scaled coordinates
                    val box = RectF(
                        scaledX1,
                        scaledY1,
                        scaledX2,
                        scaledY2
                    )
                    
                    detections.add(
                        Detection(
                            box = box,
                            confidence = confidence,
                            classId = classId
                        )
                    )
                }
            }
            
            // Sort by confidence (highest first)
            detections.sortByDescending { it.confidence }
            
            Log.d(TAG, "Parsed ${detections.size} detections above threshold $CONFIDENCE_THRESHOLD")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse detections", e)
        }
        
        return detections
    }
    
    /**
     * Step-7: Parse detections for eye regions
     * Filters detections likely to be eyes within face region
     * 
     * @param output Raw model output [1, 300, 6]
     * @param faceWidth Cropped face width
     * @param faceHeight Cropped face height
     * @return List of eye detections (up to 2: left and right)
     */
    fun parseEyes(
        output: Array<Array<FloatArray>>,
        faceWidth: Int,
        faceHeight: Int
    ): List<Detection> {
        val detections = mutableListOf<Detection>()
        
        try {
            // Iterate through detections
            for (i in 0 until 300) {
                val x1 = output[0][i][0]
                val y1 = output[0][i][1]
                val x2 = output[0][i][2]
                val y2 = output[0][i][3]
                val confidence = output[0][i][4]
                
                // Lower threshold for eyes (more lenient)
                if (confidence > EYE_CONFIDENCE_THRESHOLD) {
                    // Scale to face coordinates
                    val scaleX = faceWidth.toFloat() / 640f
                    val scaleY = faceHeight.toFloat() / 640f
                    
                    val scaledX1 = x1 * scaleX
                    val scaledY1 = y1 * scaleY
                    val scaledX2 = x2 * scaleX
                    val scaledY2 = y2 * scaleY
                    
                    // Filter: eyes should be in upper 60% of face
                    val centerY = (scaledY1 + scaledY2) / 2f
                    if (centerY < faceHeight * 0.6) {
                        val box = RectF(scaledX1, scaledY1, scaledX2, scaledY2)
                        detections.add(Detection(box, confidence))
                    }
                }
            }
            
            // Sort by confidence and take top 2
            detections.sortByDescending { it.confidence }
            val topTwo = detections.take(2).sortedBy { it.box.centerX() }
            
            Log.d(TAG, "Eye detections: ${topTwo.size} eyes found")
            
            return topTwo
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse eye detections", e)
            return emptyList()
        }
    }
    
    /**
     * Convert Detection to app-compatible DetectionResult
     * Used for storing in AppState
     */
    fun toDetectionResult(detection: Detection, index: Int): com.org.humanfaceeyedetector.state.DetectionResult {
        return com.org.humanfaceeyedetector.state.DetectionResult(
            faceId = index,
            confidence = detection.confidence,
            x1 = detection.box.left,
            y1 = detection.box.top,
            x2 = detection.box.right,
            y2 = detection.box.bottom
        )
    }
}
