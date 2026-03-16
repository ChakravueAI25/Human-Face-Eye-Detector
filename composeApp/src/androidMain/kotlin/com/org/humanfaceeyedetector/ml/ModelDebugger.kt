package com.org.humanfaceeyedetector.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "ModelDebugger"

/**
 * Debug utility for face detection model
 * 
 * Provides detailed logging and debugging information for:
 * - Image preprocessing
 * - Model inference
 * - Detection parsing
 * - Result validation
 */
object ModelDebugger {
    
    private val debugLogs = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    
    /**
     * Log a debug message with timestamp
     */
    fun debug(message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $message"
        debugLogs.add(logEntry)
        Log.d(TAG, message)
    }
    
    /**
     * Log an error message with timestamp
     */
    fun error(message: String, exception: Exception? = null) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] ERROR: $message"
        debugLogs.add(logEntry)
        if (exception != null) {
            Log.e(TAG, message, exception)
        } else {
            Log.e(TAG, message)
        }
    }
    
    /**
     * Debug image preprocessing
     */
    fun debugPreprocessing(bitmap: Bitmap, inputBuffer: java.nio.ByteBuffer) {
        debug("=== IMAGE PREPROCESSING DEBUG ===")
        debug("Original image size: ${bitmap.width}x${bitmap.height}")
        debug("Original image config: ${bitmap.config}")
        debug("Original image bytes: ${bitmap.byteCount}")
        debug("Input buffer size: ${inputBuffer.capacity()} bytes")
        debug("Expected buffer size: ${1 * 640 * 640 * 3 * 4} bytes (1 batch, 640x640, 3 channels, 4 bytes float)")
        
        if (inputBuffer.capacity() != 1 * 640 * 640 * 3 * 4) {
            error("Buffer size mismatch! Expected ${1 * 640 * 640 * 3 * 4}, got ${inputBuffer.capacity()}")
        }
    }
    
    /**
     * Debug inference output
     */
    fun debugInferenceOutput(output: Array<Array<FloatArray>>) {
        debug("=== INFERENCE OUTPUT DEBUG ===")
        debug("Output shape: [${output.size}, ${output[0].size}, ${output[0][0].size}]")
        debug("Expected shape: [1, 300, 6]")
        
        // Check if output shape is correct
        if (output.size != 1 || output[0].size != 300 || output[0][0].size != 6) {
            error("Output shape mismatch!")
        }
        
        // Find detections with non-zero values
        var nonZeroCount = 0
        var maxConfidence = 0f
        var detectionCount = 0
        
        for (i in 0 until 300) {
            val x1 = output[0][i][0]
            val y1 = output[0][i][1]
            val x2 = output[0][i][2]
            val y2 = output[0][i][3]
            val confidence = output[0][i][4]
            val classId = output[0][i][5]
            
            // Check if any value is non-zero
            if (x1 != 0f || y1 != 0f || x2 != 0f || y2 != 0f || confidence != 0f || classId != 0f) {
                nonZeroCount++
                
                if (confidence > 0f) {
                    detectionCount++
                    maxConfidence = maxOf(maxConfidence, confidence)
                    
                    if (detectionCount <= 5) {  // Log first 5 detections
                        debug("Detection $detectionCount: box=($x1, $y1, $x2, $y2), confidence=$confidence, classId=$classId")
                    }
                }
            }
        }
        
        debug("Non-zero outputs: $nonZeroCount / 300")
        debug("Detections with confidence > 0: $detectionCount")
        debug("Max confidence found: $maxConfidence")
        
        // Check if output is all zeros (indicating model not working)
        if (nonZeroCount == 0) {
            error("Model output is all ZEROS! This indicates the model is not running inference properly.")
        }
    }
    
    /**
     * Debug detection parsing with thresholds
     */
    fun debugDetectionParsing(
        output: Array<Array<FloatArray>>,
        imageWidth: Int,
        imageHeight: Int,
        confidenceThreshold: Float = 0.45f
    ) {
        debug("=== DETECTION PARSING DEBUG ===")
        debug("Confidence threshold: $confidenceThreshold")
        debug("Image dimensions: ${imageWidth}x${imageHeight}")
        debug("Scale factors: scaleX=${imageWidth / 640f}, scaleY=${imageHeight / 640f}")
        
        var detectionCount = 0
        var filteredCount = 0
        val detectionDetails = mutableListOf<String>()
        
        for (i in 0 until 300) {
            val confidence = output[0][i][4]
            
            if (confidence > 0f) {
                detectionCount++
                
                if (confidence > confidenceThreshold) {
                    filteredCount++
                    
                    val x1 = output[0][i][0]
                    val y1 = output[0][i][1]
                    val x2 = output[0][i][2]
                    val y2 = output[0][i][3]
                    
                    if (filteredCount <= 10) {  // Log first 10 filtered detections
                        detectionDetails.add(
                            "Detection $filteredCount (index $i): " +
                            "box=(${x1.toInt()}, ${y1.toInt()}, ${x2.toInt()}, ${y2.toInt()}), " +
                            "confidence=$confidence"
                        )
                    }
                }
            }
        }
        
        debug("Total detections with confidence > 0: $detectionCount")
        debug("Detections above threshold ($confidenceThreshold): $filteredCount")
        
        detectionDetails.forEach { debug(it) }
        
        if (filteredCount == 0 && detectionCount > 0) {
            debug("WARNING: Found detections but all below threshold. Consider lowering confidence threshold.")
        }
    }
    
    /**
     * Try different confidence thresholds to find valid detections
     */
    fun debugFindValidThreshold(output: Array<Array<FloatArray>>) {
        debug("=== THRESHOLD ANALYSIS ===")
        
        val thresholds = listOf(0.1f, 0.2f, 0.3f, 0.35f, 0.45f, 0.5f, 0.6f, 0.7f, 0.8f)
        val detectionCounts = mutableMapOf<Float, Int>()
        
        for (threshold in thresholds) {
            var count = 0
            for (i in 0 until 300) {
                if (output[0][i][4] > threshold) {
                    count++
                }
            }
            detectionCounts[threshold] = count
            debug("Threshold $threshold: $count detections")
        }
        
        val maxDetections = detectionCounts.maxByOrNull { it.value }
        if (maxDetections != null && maxDetections.value > 0) {
            debug("✓ Recommendation: Consider using threshold ${maxDetections.key} to get ${maxDetections.value} detections")
        }
    }
    
    /**
     * Comprehensive model health check
     */
    fun performHealthCheck(context: Context): HealthCheckResult {
        val result = HealthCheckResult()
        
        debug("=== MODEL HEALTH CHECK ===")
        
        try {
            // Check 1: Model file exists
            val modelFile = File(context.cacheDir.parent, "models/model_fp32.tflite")
            result.modelFileExists = modelFile.exists()
            debug("✓ Model file exists: ${result.modelFileExists}")
            
            if (result.modelFileExists) {
                result.modelFileSize = modelFile.length()
                debug("✓ Model file size: ${result.modelFileSize} bytes")
            }
            
            // Check 2: Try loading model
            val modelRunner = ModelRunnerHolder.getInstance()
            result.modelLoaded = modelRunner != null
            debug("✓ Model loaded: ${result.modelLoaded}")
            
            // Check 3: Sample preprocessing
            val sampleBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
            val preprocessed = ImageProcessor.preprocess(sampleBitmap)
            result.preprocessingWorks = preprocessed.capacity() > 0
            debug("✓ Preprocessing works: ${result.preprocessingWorks}")
            
            result.status = "HEALTHY"
            
        } catch (e: Exception) {
            error("Health check failed", e)
            result.status = "ERROR"
            result.errorMessage = e.message ?: "Unknown error"
        }
        
        return result
    }
    
    /**
     * Export debug logs to file
     */
    fun exportLogs(context: Context): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val logsFile = File(context.cacheDir, "debug_logs_$timestamp.txt")
            
            logsFile.writeText(debugLogs.joinToString("\n"))
            debug("✓ Logs exported to: ${logsFile.absolutePath}")
            logsFile
            
        } catch (e: Exception) {
            error("Failed to export logs", e)
            null
        }
    }
    
    /**
     * Clear debug logs
     */
    fun clearLogs() {
        debugLogs.clear()
        debug("Debug logs cleared")
    }
    
    /**
     * Get all debug logs as string
     */
    fun getLogs(): String = debugLogs.joinToString("\n")
    
    data class HealthCheckResult(
        var status: String = "UNKNOWN",
        var modelFileExists: Boolean = false,
        var modelFileSize: Long = 0L,
        var modelLoaded: Boolean = false,
        var preprocessingWorks: Boolean = false,
        var errorMessage: String = ""
    )
}

