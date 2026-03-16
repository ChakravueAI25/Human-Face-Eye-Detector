package com.org.humanfaceeyedetector.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File

private const val TAG = "ModelTestRunner"

/**
 * Standalone test runner for validating the face detection model
 * 
 * Usage:
 * ```
 * val testRunner = ModelTestRunner(context)
 * testRunner.testWithImage(imageBitmap)
 * val report = testRunner.generateReport()
 * Log.d("TEST", report)
 * ```
 */
class ModelTestRunner(private val context: Context) {
    
    private val testResults = mutableListOf<String>()
    private var testStartTime = 0L
    
    /**
     * Run complete test suite on an image
     */
    fun testWithImage(bitmap: Bitmap): TestResult {
        testStartTime = System.currentTimeMillis()
        testResults.clear()
        
        log("╔══════════════════════════════════════╗")
        log("║      MODEL TEST SUITE STARTED        ║")
        log("╚══════════════════════════════════════╝")
        
        // Test 1: Model loading
        testModelLoading()
        
        // Test 2: Image preprocessing
        testImagePreprocessing(bitmap)
        
        // Test 3: Inference
        val inferenceResult = testInference(bitmap)
        
        // Test 4: Detection parsing
        testDetectionParsing(inferenceResult)
        
        val duration = System.currentTimeMillis() - testStartTime
        log("Total test duration: ${duration}ms")
        
        return TestResult(
            passed = testResults.filter { it.contains("✓") }.size,
            failed = testResults.filter { it.contains("✗") }.size,
            logs = testResults
        )
    }
    
    /**
     * Test 1: Model Loading
     */
    private fun testModelLoading() {
        log("\n[TEST 1] Model Loading")
        
        try {
            // Initialize model if not already initialized
            if (ModelRunnerHolder.getInstance() == null) {
                ModelRunnerHolder.initialize(context)
                log("✓ Model runner initialized")
            }

            val modelRunner = ModelRunnerHolder.getInstance()
            
            if (modelRunner != null) {
                log("✓ Model loaded successfully")
                modelRunner.debugHealthCheck(context)
            } else {
                log("✗ Model failed to load (getInstance returned null)")
            }
            
        } catch (e: Exception) {
            log("✗ Exception during model loading: ${e.message}")
        }
    }
    
    /**
     * Test 2: Image Preprocessing
     */
    private fun testImagePreprocessing(bitmap: Bitmap) {
        log("\n[TEST 2] Image Preprocessing")
        
        try {
            log("  Input: ${bitmap.width}x${bitmap.height} (${bitmap.config})")
            
            val buffer = ImageProcessor.preprocess(bitmap)
            
            val expectedSize = 1 * 640 * 640 * 3 * 4  // [1, 640, 640, 3] float32
            
            if (buffer.capacity() == expectedSize) {
                log("✓ Buffer size correct: ${buffer.capacity()} bytes")
            } else {
                log("✗ Buffer size mismatch: ${buffer.capacity()} vs expected $expectedSize")
            }
            
            log("✓ Preprocessing completed without errors")
            
        } catch (e: Exception) {
            log("✗ Preprocessing failed: ${e.message}")
        }
    }
    
    /**
     * Test 3: Inference
     */
    private fun testInference(bitmap: Bitmap): Array<Array<FloatArray>>? {
        log("\n[TEST 3] Inference")
        
        return try {
            val modelRunner = ModelRunnerHolder.getInstance()
            
            if (modelRunner == null) {
                log("✗ Model runner not initialized")
                return null
            }
            
            val start = System.currentTimeMillis()
            
            // Run inference with debug enabled
            val detections = modelRunner.detect(
                bitmap = bitmap,
                originalWidth = bitmap.width,
                originalHeight = bitmap.height,
                enableDebug = true
            )
            
            val duration = System.currentTimeMillis() - start
            
            log("✓ Inference completed in ${duration}ms")
            log("✓ Detections found: ${detections.size}")
            
            modelRunner.getLastOutput()
            
        } catch (e: Exception) {
            log("✗ Inference failed: ${e.message}")
            null
        }
    }
    
    /**
     * Test 4: Detection Parsing
     */
    private fun testDetectionParsing(output: Array<Array<FloatArray>>?) {
        log("\n[TEST 4] Detection Parsing")
        
        if (output == null) {
            log("✗ No output to parse (inference failed)")
            return
        }
        
        try {
            // Check output shape
            if (output.size != 1 || output[0].size != 300 || output[0][0].size != 6) {
                log("✗ Wrong output shape: [${output.size}, ${output[0].size}, ${output[0][0].size}]")
                return
            }
            log("✓ Output shape correct: [1, 300, 6]")
            
            // Count non-zero outputs
            var nonZeroCount = 0
            var detectionCount = 0
            var maxConfidence = 0f
            
            for (i in 0 until 300) {
                val confidence = output[0][i][4]
                
                if (confidence > 0f) {
                    nonZeroCount++
                    detectionCount++
                    maxConfidence = maxOf(maxConfidence, confidence)
                }
            }
            
            if (nonZeroCount > 0) {
                log("✓ Non-zero outputs: $nonZeroCount")
            } else {
                log("✗ All outputs are ZERO (model not working)")
            }
            
            if (detectionCount > 0) {
                log("✓ Detections found: $detectionCount (max confidence: $maxConfidence)")
            } else {
                log("✗ No detections (model returned zeros)")
            }
            
            // Test different thresholds
            log("\n  Testing different confidence thresholds:")
            listOf(0.1f, 0.2f, 0.3f, 0.35f, 0.45f, 0.5f).forEach { threshold ->
                var count = 0
                for (i in 0 until 300) {
                    if (output[0][i][4] > threshold) count++
                }
                log("    Threshold $threshold: $count detections")
            }
            
        } catch (e: Exception) {
            log("✗ Parsing failed: ${e.message}")
        }
    }
    
    /**
     * Generate final test report
     */
    fun generateReport(): String {
        return buildString {
            appendLine("\n╔══════════════════════════════════════╗")
            appendLine("║        TEST REPORT GENERATED         ║")
            appendLine("╚══════════════════════════════════════╝")
            appendLine()
            testResults.forEach { appendLine(it) }
            appendLine()
            appendLine("═════════════════════════════════════")
        }
    }
    
    /**
     * Save report to file
     */
    fun saveReportToFile(): File? {
        return try {
            val reportFile = File(context.cacheDir, "model_test_report.txt")
            reportFile.writeText(generateReport())
            Log.d(TAG, "Report saved to: ${reportFile.absolutePath}")
            reportFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save report", e)
            null
        }
    }
    
    private fun log(message: String) {
        testResults.add(message)
        Log.d(TAG, message)
    }
    
    data class TestResult(
        val passed: Int,
        val failed: Int,
        val logs: List<String>
    )
}

/**
 * Quick test function for debugging
 * Call this from your app to test the model
 */
fun debugTestModel(context: Context, testBitmap: Bitmap) {
    val testRunner = ModelTestRunner(context)
    val result = testRunner.testWithImage(testBitmap)
    
    Log.d("MODEL_TEST", "═══════════════════════════════════")
    Log.d("MODEL_TEST", "TESTS PASSED: ${result.passed}")
    Log.d("MODEL_TEST", "TESTS FAILED: ${result.failed}")
    Log.d("MODEL_TEST", "═══════════════════════════════════")
    
    result.logs.forEach { Log.d("MODEL_TEST", it) }
    
    testRunner.saveReportToFile()
}
