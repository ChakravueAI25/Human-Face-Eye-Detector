package com.org.humanfaceeyedetector.ml

import android.graphics.RectF
import android.util.Log

private const val TAG = "DetectionParser"
private const val CONFIDENCE_THRESHOLD = 0.35f
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
        
        Log.d("MODEL_DEBUG", "Image size: $imageWidth x $imageHeight")
        Log.d("MODEL_DEBUG", "Total detections from model: ${output[0].size}")

        try {
            // Iterate through all predictions (dynamic size, usually 8400 or 300)
            for (i in output[0].indices) {
                // Correct Format: [x1, y1, x2, y2, confidence, class_id]
                // Coordinates are normalized 0-1
                
                val x1 = output[0][i][0]
                val y1 = output[0][i][1]
                val x2 = output[0][i][2]
                val y2 = output[0][i][3]
                val confidence = output[0][i][4]
                val classId = output[0][i][5].toInt()
                
                // Filter by class (only accept class 0 = face/person)
                if (classId != 0) continue

                // Filter by confidence threshold
                if (confidence > CONFIDENCE_THRESHOLD) {
                    
                    // Scale normalized coordinates (0-1) to image dimensions
                    val left = x1 * imageWidth
                    val top = y1 * imageHeight
                    val right = x2 * imageWidth
                    val bottom = y2 * imageHeight

                    // Clamp to image bounds
                    val clampedLeft = left.coerceAtLeast(0f)
                    val clampedTop = top.coerceAtLeast(0f)
                    val clampedRight = right.coerceAtMost(imageWidth.toFloat())
                    val clampedBottom = bottom.coerceAtMost(imageHeight.toFloat())
                    
                    // Simple size check to avoid empty boxes
                    if (clampedRight <= clampedLeft || clampedBottom <= clampedTop) continue

                    Log.d(
                        "MODEL_DEBUG",
                        "BOX: l=$clampedLeft t=$clampedTop r=$clampedRight b=$clampedBottom conf=$confidence"
                    )
                    
                    val box = RectF(clampedLeft, clampedTop, clampedRight, clampedBottom)
                    
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
            Log.d("MODEL_DEBUG", "Total detections returned: ${detections.size}")
            
            // NMS is already applied by the model export, so we skip it here.
            return detections
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse detections", e)
        }
        
        return detections
    }

    /**
     * Non-Maximum Suppression to remove overlapping bounding boxes
     */
    private fun nonMaxSuppression(
        detections: List<Detection>,
        iouThreshold: Float = 0.45f
    ): List<Detection> {
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val selected = mutableListOf<Detection>()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            selected.add(best)

            val iterator = sorted.iterator()

            while (iterator.hasNext()) {
                val other = iterator.next()

                if (calculateIoU(best.box, other.box) > iouThreshold) {
                    iterator.remove()
                }
            }
        }

        return selected
    }

    /**
     * Calculate Intersection over Union (IoU) between two boxes
     */
    private fun calculateIoU(a: RectF, b: RectF): Float {
        val intersectionLeft = kotlin.math.max(a.left, b.left)
        val intersectionTop = kotlin.math.max(a.top, b.top)
        val intersectionRight = kotlin.math.min(a.right, b.right)
        val intersectionBottom = kotlin.math.min(a.bottom, b.bottom)

        val intersectionArea =
            kotlin.math.max(0f, intersectionRight - intersectionLeft) *
            kotlin.math.max(0f, intersectionBottom - intersectionTop)

        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)

        val union = areaA + areaB - intersectionArea

        return if (union <= 0) 0f else intersectionArea / union
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
        // TEMPORARY: Approximate eye regions using face geometry
        // Until dedicated landmark model is integrated
        val detections = mutableListOf<Detection>()
        
        try {
            // Left eye region: 15% from left, 25% from top
            val leftEyeX = 0.15f * faceWidth
            val leftEyeY = 0.25f * faceHeight
            val eyeW = 0.30f * faceWidth
            val eyeH = 0.20f * faceHeight
            
            detections.add(
                Detection(
                    box = RectF(leftEyeX, leftEyeY, leftEyeX + eyeW, leftEyeY + eyeH),
                    confidence = 0.90f
                )
            )
            
            // Right eye region: 55% from left, 25% from top
            val rightEyeX = 0.55f * faceWidth
            val rightEyeY = 0.25f * faceHeight
            
            detections.add(
                Detection(
                    box = RectF(rightEyeX, rightEyeY, rightEyeX + eyeW, rightEyeY + eyeH),
                    confidence = 0.90f
                )
            )
            
            Log.d(TAG, "Generated 2 heuristic eye regions")
            return detections
            
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
