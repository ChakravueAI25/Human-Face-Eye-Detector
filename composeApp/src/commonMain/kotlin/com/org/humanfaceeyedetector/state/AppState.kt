package com.org.humanfaceeyedetector.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import com.org.humanfaceeyedetector.getCurrentTimeMillis

sealed class Screen {
    data object Splash : Screen()
    data object Home : Screen()
    data object Camera : Screen()
    data object FaceSelection : Screen()
    data object EyeSelection : Screen()
    data object Result : Screen()
}

enum class EyeType {
    Left,
    Right
}

/**
 * Step-5: Detection result from ML model
 * Stores bounding box coordinates and confidence score
 */
data class DetectionResult(
    val faceId: Int,
    val confidence: Float,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)

/**
 * Step-5/8: Application state includes ML inference results
 * Separates captured image from detection results
 * UI should only render, not compute
 */
data class AppStateData(
    val currentScreen: Screen = Screen.Splash,
    val selectedFace: Int? = null,
    val selectedEye: EyeType? = null,
    val capturedImage: ImageBitmap? = null,
    val captureTimestamp: Long? = null,  // null = no capture yet, Long = milliseconds since epoch
    // Step-5: ML inference results
    val detections: List<DetectionResult> = emptyList(),
    val eyeDetections: List<DetectionResult> = emptyList(),
    val isProcessing: Boolean = false,
    val inferenceError: String? = null
)

class AppStateHolder {
    private val _state = mutableStateOf(AppStateData())
    
    val state: AppStateData
        get() = _state.value
    
    fun navigateTo(screen: Screen) {
        _state.value = _state.value.copy(currentScreen = screen)
    }
    
    fun selectFace(faceId: Int) {
        _state.value = _state.value.copy(selectedFace = faceId)
    }
    
    fun selectEye(eye: EyeType) {
        _state.value = _state.value.copy(selectedEye = eye)
    }
    
    fun setCapturedImage(image: ImageBitmap?) {
        _state.value = _state.value.copy(
            capturedImage = image,
            captureTimestamp = if (image != null) getCurrentTimeMillis() else null
        )
    }
    
    // Step-5: ML inference state management
    fun setProcessing(processing: Boolean) {
        _state.value = _state.value.copy(isProcessing = processing)
    }
    
    fun setDetections(detections: List<DetectionResult>) {
        _state.value = _state.value.copy(
            detections = detections,
            isProcessing = false,
            inferenceError = null
        )
    }
    
    // Step-7: Eye detection state management
    fun setEyeDetections(eyeDetections: List<DetectionResult>) {
        _state.value = _state.value.copy(
            eyeDetections = eyeDetections,
            isProcessing = false,
            inferenceError = null
        )
    }
    
    fun setInferenceError(error: String) {
        _state.value = _state.value.copy(
            inferenceError = error,
            isProcessing = false
        )
    }
    
    fun reset() {
        _state.value = AppStateData(currentScreen = Screen.Home)
    }
}

@Composable
fun rememberAppState(): AppStateHolder {
    return remember { AppStateHolder() }
}
