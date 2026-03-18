package com.org.humanfaceeyedetector.navigation

import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import com.org.humanfaceeyedetector.ml.EyeDetectionManager
import com.org.humanfaceeyedetector.ml.InferenceManager
import com.org.humanfaceeyedetector.ml.cropFace
import com.org.humanfaceeyedetector.state.AppStateHolder
import com.org.humanfaceeyedetector.state.Screen
import com.org.humanfaceeyedetector.ui.SplashScreen
import com.org.humanfaceeyedetector.ui.HomeScreen
import com.org.humanfaceeyedetector.ui.CameraScreen
import com.org.humanfaceeyedetector.ui.FaceDetectionScreen
import com.org.humanfaceeyedetector.ui.EyeDetectionScreen
import com.org.humanfaceeyedetector.ui.ResultScreen

@Composable
actual fun AppNavigation(appState: AppStateHolder) {
    val context = LocalContext.current
    
    // Setup image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                val imageBitmap = bitmap.asImageBitmap()
                // Clear previous detections to trigger new inference
                appState.setDetections(emptyList())
                appState.setCapturedImage(imageBitmap)
                appState.navigateTo(Screen.FaceSelection)
            } catch (e: Exception) {
                appState.setInferenceError("Failed to load image: ${e.message}")
            }
        }
    }
    
    when (appState.state.currentScreen) {
        Screen.Splash -> SplashScreen(
            onFinished = { appState.navigateTo(Screen.Home) }
        )
        
        Screen.Home -> HomeScreen(
            onCapture = { appState.navigateTo(Screen.Camera) },
            onUploadClick = { 
                imagePickerLauncher.launch("image/*")
            }
        )
        
        Screen.Camera -> CameraScreen(
            appState = appState,
            onBack = { appState.navigateTo(Screen.Home) },
            onCapture = { imageBitmap ->
                // Store captured image in state
                appState.setCapturedImage(imageBitmap)
                // Navigate to face selection
                appState.navigateTo(Screen.FaceSelection)
            }
        )
        
        Screen.FaceSelection -> {
            // Step-6: Trigger ML inference when image is captured
            LaunchedEffect(appState.state.capturedImage) {
                // Also trigger if detections are empty (handled by launcher clearing it)
                if (appState.state.capturedImage != null && appState.state.detections.isEmpty() && !appState.state.isProcessing) {
                    InferenceManager.detectFaces(appState.state.capturedImage!!, appState)
                }
            }
            
            FaceDetectionScreen(
                capturedImage = appState.state.capturedImage,
                selectedFace = appState.state.selectedFace,
                detections = appState.state.detections,
                isProcessing = appState.state.isProcessing,
                onBack = { appState.navigateTo(Screen.Home) },
                onFaceSelected = { faceId ->
                    appState.selectFace(faceId)
                    // Clear previous eye detections when selecting a new face
                    appState.setEyeDetections(emptyList())
                    appState.navigateTo(Screen.EyeSelection)
                }
            )
        }
        
        Screen.EyeSelection -> {
            // Step-7: Trigger eye detection when face is selected
            LaunchedEffect(appState.state.selectedFace, appState.state.capturedImage) {
                if (appState.state.selectedFace != null && 
                    appState.state.capturedImage != null && 
                    appState.state.eyeDetections.isEmpty() && 
                    !appState.state.isProcessing) {
                    EyeDetectionManager.detectEyes(
                        appState.state.capturedImage!!,
                        appState.state.selectedFace!!,
                        appState
                    )
                }
            }
            
            // Calculate crops for display
            val capturedImage = appState.state.capturedImage
            val selectedFaceId = appState.state.selectedFace
            val faceDetection = if (selectedFaceId != null) appState.state.detections.getOrNull(selectedFaceId) else null
            
            val faceImage = if (capturedImage != null && faceDetection != null) {
                try {
                    // Convert to Android Bitmap for cropping
                    val androidBitmap = capturedImage.asAndroidBitmap()
                    
                    cropFace(
                        androidBitmap,
                        faceDetection.x1,
                        faceDetection.y1,
                        faceDetection.x2,
                        faceDetection.y2
                    ).asImageBitmap()
                } catch (e: Exception) {
                    Log.e("Navigator", "Face crop failed", e)
                    null
                }
            } else null
            
            EyeDetectionScreen(
                faceImage = faceImage,
                faceDetection = faceDetection,
                selectedEye = appState.state.selectedEye,
                eyeDetections = appState.state.eyeDetections,
                isProcessing = appState.state.isProcessing,
                onBack = { 
                    // Clear eye selection when going back to face selection
                    appState.setEyeDetections(emptyList())
                    appState.navigateTo(Screen.FaceSelection) 
                },
                onEyeSelected = { eye ->
                    appState.selectEye(eye)
                    appState.navigateTo(Screen.Result)
                }
            )
        }
        
        Screen.Result -> {
            // Calculate eye crop for result
            val capturedImage = appState.state.capturedImage
            val selectedFace = appState.state.selectedFace
            val selectedEye = appState.state.selectedEye
            
            val eyeDetection = if (selectedEye != null) {
                val index = if (selectedEye == com.org.humanfaceeyedetector.state.EyeType.Left) 0 else 1
                appState.state.eyeDetections.getOrNull(index)
            } else null
            
            val eyeImage = if (capturedImage != null && eyeDetection != null) {
                 try {
                    // Convert to Android Bitmap for cropping
                    val androidBitmap = capturedImage.asAndroidBitmap()
                    
                    cropFace(
                        androidBitmap,
                        eyeDetection.x1,
                        eyeDetection.y1,
                        eyeDetection.x2,
                        eyeDetection.y2
                    ).asImageBitmap()
                } catch (e: Exception) {
                    Log.e("Navigator", "Eye crop failed", e)
                    null
                }
            } else null

            ResultScreen(
                eyeImage = eyeImage,
                selectedFace = selectedFace,
                selectedEye = selectedEye,
                captureTimestamp = appState.state.captureTimestamp,
                onBack = { appState.navigateTo(Screen.EyeSelection) },
                onNewScan = { 
                    appState.reset()
                    appState.navigateTo(Screen.Home)
                }
            )
        }
    }
}
