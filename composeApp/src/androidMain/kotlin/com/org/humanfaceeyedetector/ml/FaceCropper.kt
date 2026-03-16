package com.org.humanfaceeyedetector.ml

import android.graphics.Bitmap
import android.util.Log
import kotlin.math.max
import kotlin.math.min

private const val TAG = "FaceCropper"

/**
 * Step-7: Utility to crop face region from image
 * Handles boundary clamping to prevent out-of-bounds crashes
 */
object FaceCropper {
    
    /**
     * Crop face region from bitmap
     * Clamps coordinates to image bounds
     * 
     * @param bitmap Original image
     * @param left Face box left coordinate
     * @param top Face box top coordinate
     * @param right Face box right coordinate
     * @param bottom Face box bottom coordinate
     * @return Cropped bitmap of face region
     */
    fun cropFace(
        bitmap: Bitmap,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): Bitmap {
        return try {
            // Clamp coordinates to image bounds
            val clampedLeft = max(0f, left).toInt()
            val clampedTop = max(0f, top).toInt()
            val clampedRight = min(bitmap.width.toFloat(), right).toInt()
            val clampedBottom = min(bitmap.height.toFloat(), bottom).toInt()
            
            // Ensure width and height are positive
            val width = maxOf(1, clampedRight - clampedLeft)
            val height = maxOf(1, clampedBottom - clampedTop)
            
            Log.d(TAG, "Cropping face: left=$clampedLeft, top=$clampedTop, width=$width, height=$height")
            
            // Create cropped bitmap
            val cropped = Bitmap.createBitmap(bitmap, clampedLeft, clampedTop, width, height)
            Log.d(TAG, "Face cropped successfully: ${cropped.width}x${cropped.height}")
            cropped
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to crop face", e)
            // Return original bitmap on error
            bitmap
        }
    }
}

