package com.org.humanfaceeyedetector.navigation

import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.org.humanfaceeyedetector.ml.EyeDetectionManager
import com.org.humanfaceeyedetector.ml.InferenceManager
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
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                val imageBitmap = bitmap.asImageBitmap()
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
            
            EyeDetectionScreen(
                selectedFace = appState.state.selectedFace,
                selectedEye = appState.state.selectedEye,
                eyeDetections = appState.state.eyeDetections,
                isProcessing = appState.state.isProcessing,
                onBack = { appState.navigateTo(Screen.FaceSelection) },
                onEyeSelected = { eye ->
                    appState.selectEye(eye)
                    appState.navigateTo(Screen.Result)
                }
            )
        }
        
        Screen.Result -> ResultScreen(
            capturedImage = appState.state.capturedImage,
            selectedFace = appState.state.selectedFace,
            selectedEye = appState.state.selectedEye,
            captureTimestamp = appState.state.captureTimestamp,
            detections = appState.state.detections,
            eyeDetections = appState.state.eyeDetections,
            onBack = { appState.navigateTo(Screen.EyeSelection) },
            onNewScan = { 
                appState.reset()
                appState.navigateTo(Screen.Home)
            }
        )
    }
}
