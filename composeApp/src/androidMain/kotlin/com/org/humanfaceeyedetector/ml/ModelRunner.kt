package com.org.humanfaceeyedetector.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

private const val TAG = "ModelRunner"
private const val MODEL_PATH = "models/model_fp32.tflite"
private const val INPUT_SIZE = 640

/**
 * Step-6: ModelRunner handles TensorFlow Lite inference
 * 
 * Responsibilities:
 * - Load model from assets
 * - Preprocess captured bitmap
 * - Run inference
 * - Parse detection results
 */
class ModelRunner(context: Context) {
    private var interpreter: Interpreter? = null
    private val context = context
    private var lastOutput: Array<Array<FloatArray>>? = null
    
    init {
        try {
            // Load TFLite model from assets
            val modelBuffer = loadModelFile(context)
            interpreter = Interpreter(modelBuffer)
            Log.d(TAG, "Model loaded successfully: $MODEL_PATH")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model", e)
        }
    }
    
    /**
     * Load model from assets into MappedByteBuffer
     */
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_PATH)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    /**
     * Run face detection on bitmap
     * Step-6: Complete inference pipeline
     * 
     * @param bitmap Captured image (any resolution)
     * @param originalWidth Original image width (for coordinate scaling)
     * @param originalHeight Original image height (for coordinate scaling)
     * @param enableDebug Enable detailed debugging output
     * @return List of detections with scaled coordinates
     */
    fun detect(
        bitmap: Bitmap,
        originalWidth: Int,
        originalHeight: Int,
        enableDebug: Boolean = false
    ): List<Detection> {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter not initialized")
            return emptyList()
        }
        
        return try {
            // Step 1: Preprocess image (resize to 640x640, normalize)
            val inputBuffer = ImageProcessor.preprocess(bitmap)
            if (enableDebug) {
                ModelDebugger.debugPreprocessing(bitmap, inputBuffer)
            }
            
            // Step 2: Prepare output buffer [1, 300, 6]
            val output = Array(1) { Array(300) { FloatArray(6) } }
            
            // Step 3: Run inference
            interpreter?.run(inputBuffer, output)
            if (enableDebug) {
                ModelDebugger.debugInferenceOutput(output)
                ModelDebugger.debugDetectionParsing(output, originalWidth, originalHeight)
                ModelDebugger.debugFindValidThreshold(output)
            }
            
            // Store output for eye detection access
            lastOutput = output
            
            // Step 4: Parse output into Detection objects
            val detections = DetectionParser.parse(output, originalWidth, originalHeight)
            
            Log.d(TAG, "Detection complete: ${detections.size} objects found")
            detections
            
        } catch (e: Exception) {
            Log.e(TAG, "Detection failed", e)
            ModelDebugger.error("Detection failed", e)
            emptyList()
        }
    }
    
    /**
     * Get last inference output (for eye detection)
     */
    fun getLastOutput(): Array<Array<FloatArray>>? = lastOutput
    
    /**
     * Perform comprehensive health check and debugging
     */
    fun debugHealthCheck(context: Context) {
        val result = ModelDebugger.performHealthCheck(context)
        Log.d(TAG, "Health check: ${result.status}")
        Log.d(TAG, "Model loaded: ${result.modelLoaded}")
        Log.d(TAG, "Preprocessing works: ${result.preprocessingWorks}")
        if (result.errorMessage.isNotEmpty()) {
            Log.e(TAG, "Health check error: ${result.errorMessage}")
        }
    }
    
    /**
     * Export debug logs to file for inspection
     */
    fun exportDebugLogs(context: Context): String? {
        return ModelDebugger.exportLogs(context)?.absolutePath
    }
    
    /**
     * Get all debug logs as string
     */
    fun getDebugLogs(): String = ModelDebugger.getLogs()
    
    /**
     * Clear debug logs
     */
    fun clearDebugLogs() {
        ModelDebugger.clearLogs()
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "ModelRunner closed")
    }
}

/**
 * Singleton holder for ModelRunner
 * Ensures single instance across app
 */
object ModelRunnerHolder {
    private var instance: ModelRunner? = null
    
    fun initialize(context: Context) {
        if (instance == null) {
            instance = ModelRunner(context)
            Log.d(TAG, "ModelRunner initialized")
        }
    }
    
    fun getInstance(): ModelRunner? = instance
    
    fun close() {
        instance?.close()
        instance = null
    }
}
