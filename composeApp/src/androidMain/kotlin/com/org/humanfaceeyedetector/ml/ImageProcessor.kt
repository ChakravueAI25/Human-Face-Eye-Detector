package com.org.humanfaceeyedetector.ml

import android.graphics.Bitmap
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "ImageProcessor"
private const val INPUT_SIZE = 640

/**
 * Step-6: Preprocesses captured bitmap for TensorFlow Lite inference
 * 
 * Pipeline:
 * 1. Resize bitmap to 640×640
 * 2. Extract RGB pixels
 * 3. Normalize to [0, 1] range (pixel / 255f)
 * 4. Pack into ByteBuffer with shape [1, 640, 640, 3]
 */
object ImageProcessor {
    
    /**
     * Preprocess bitmap for TFLite model
     * 
     * @param bitmap Input bitmap (any size, will be resized)
     * @return ByteBuffer with normalized pixel values in shape [1, 640, 640, 3]
     */
    fun preprocess(bitmap: Bitmap): ByteBuffer {
        return try {
            // Step 1: Resize to 640x640
            val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            
            // Step 2: Create output buffer
            // Shape: [1, 640, 640, 3] = 1 * 640 * 640 * 3 * 4 bytes (float32)
            val byteBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
            byteBuffer.order(ByteOrder.nativeOrder())

            Log.d("MODEL_DEBUG", "Input bitmap size: ${bitmap.width} x ${bitmap.height}")
            Log.d("MODEL_DEBUG", "Resized bitmap size: ${resized.width} x ${resized.height}")
            Log.d("MODEL_DEBUG", "ByteBuffer capacity: ${byteBuffer.capacity()}")

            // Step 3: Extract RGB pixels and normalize
            val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
            resized.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
            
            var pixelIndex = 0
            for (i in 0 until INPUT_SIZE) {
                for (j in 0 until INPUT_SIZE) {
                    val pixel = intValues[pixelIndex++]
                    
                    // Extract RGB components (ignore alpha)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    
                    // Normalize to [0, 1] range
                    byteBuffer.putFloat(r.toFloat() / 255f)
                    byteBuffer.putFloat(g.toFloat() / 255f)
                    byteBuffer.putFloat(b.toFloat() / 255f)

                    if (i == 0 && j == 0) {
                        Log.d("MODEL_DEBUG", "First pixel RGB normalized: R=${r/255f} G=${g/255f} B=${b/255f}")
                    }
                }
            }
            
            byteBuffer.rewind()
            Log.d(TAG, "Image preprocessed: ${INPUT_SIZE}x${INPUT_SIZE}, buffer size: ${byteBuffer.remaining()} bytes")
            byteBuffer
            
        } catch (e: Exception) {
            Log.e(TAG, "Preprocessing failed", e)
            // Return empty buffer on error
            ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        }
    }
}
