package com.org.humanfaceeyedetector.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.org.humanfaceeyedetector.camera.CameraLens
import com.org.humanfaceeyedetector.camera.CameraPreview
import com.org.humanfaceeyedetector.camera.RequestCaptureImage
import com.org.humanfaceeyedetector.navigation.AppNavigation
import com.org.humanfaceeyedetector.platform.PlatformBackHandler
import com.org.humanfaceeyedetector.state.EyeType
import com.org.humanfaceeyedetector.state.rememberAppState
import com.org.humanfaceeyedetector.formatTimestamp
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility


import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.Close

@Composable
fun App() {
    val appState = rememberAppState()
    
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
            ) {
                // Status bar spacer - adaptive to system insets
                StatusBarSpacer()
                
                // Main app content
                AppNavigation(appState)
            }
        }
    }
}

@Composable
private fun StatusBarSpacer() {
    // Standard Android status bar height is approximately 24.dp on most devices
    // This creates a dedicated spacer to keep content below the status bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Background)
    )
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    var messageIndex by remember { mutableStateOf(0) }
    
    val loadingMessages = listOf(
        "Loading model weights…",
        "Initializing AI Detection Engine",
        "Calibrating eye tracking…",
        "Ready"
    )
    
    // Prevent back button during splash
    PlatformBackHandler(enabled = true) {
        // Ignore back during splash screen
    }
    
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            progress = minOf(progress + 0.02f, 1f)
            delay(50)
            
            if (progress > 0.25f && messageIndex < 1) messageIndex = 1
            if (progress > 0.5f && messageIndex < 2) messageIndex = 2
            if (progress > 0.75f && messageIndex < 3) messageIndex = 3
        }
        delay(500)
        onFinished()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconCircle(icon = Icons.Filled.Visibility, size = 80)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        LoadingProgressCircular(progress)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "${(progress * 100).toInt()}%",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = loadingMessages[messageIndex],
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            minLines = 1
        )
    }
}

@Composable
fun HomeScreen(
    onCapture: () -> Unit,
    onUploadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(
            title = "Eye Detection"
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroCard(
                icon = Icons.Filled.Visibility,
                title = "AI-Powered Eye Detection",
                description = "Detect and analyze eyes with advanced YOLOv8 model"
            )
            
            PrimaryButton(
                text = "Capture Image",
                onClick = onCapture
            )
            
            SecondaryButton(
                text = "Upload Image",
                onClick = onUploadClick
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoCard(title = "Model Info") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetectionDetailRow("Model", "YOLOv8n")
                    DetectionDetailRow("Input", "640×640")
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        StatusDot(isActive = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ready", color = SuccessGreen, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}


@Composable
fun CameraScreen(
    appState: com.org.humanfaceeyedetector.state.AppStateHolder,
    onBack: () -> Unit,
    onCapture: (imageBitmap: androidx.compose.ui.graphics.ImageBitmap) -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var shouldCapture by remember { mutableStateOf(false) }
    var cameraLens by remember { mutableStateOf(CameraLens.Back) }
    
    // reset detections when entering screen
    LaunchedEffect(Unit) {
        appState.setDetections(emptyList())
        appState.selectFace(null)
    }

    PlatformBackHandler {
        if (appState.state.selectedFace != null) {
            appState.selectFace(null)
        } else {
            onBack()
        }
    }
    
    // Handle image capture
    if (shouldCapture) {
        RequestCaptureImage(
            onImageCaptured = { imageBitmap ->
                isProcessing = false
                shouldCapture = false
                onCapture(imageBitmap)
            },
            onError = { error ->
                isProcessing = false
                shouldCapture = false
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(
            title = if (appState.state.selectedFace != null) "Face Selected" else "Camera Capture",
            onBackClick = {
                if (appState.state.selectedFace != null) {
                    appState.selectFace(null)
                } else {
                    onBack()
                }
            }
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Live camera preview (bottom layer)
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                cameraLens = cameraLens,
                isDetectionEnabled = appState.state.selectedFace == null,
                onDetectionsUpdated = { detections ->
                     // Only update if no face is selected (freeze tracking)
                     if (appState.state.selectedFace == null) {
                         appState.setDetections(detections)
                     }
                },
                onImageDimensionsUpdated = { width, height ->
                    appState.setImageDimensions(width, height)
                }
            )
            
            // Overlay UI (top layer)
            if (appState.state.detections.isNotEmpty() && appState.state.imageWidth > 0 && appState.state.imageHeight > 0) {
                 BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                     val density = LocalDensity.current
                     val canvasWidth = with(density) { maxWidth.toPx() }
                     val canvasHeight = with(density) { maxHeight.toPx() }
                     
                     // Values from analysis (image space)
                     val imageWidth = appState.state.imageWidth.toFloat()
                     val imageHeight = appState.state.imageHeight.toFloat()
                     
                     // Step 2: Compute scale (FIT CENTER logic)
                     val scale = minOf(
                         canvasWidth / imageWidth,
                         canvasHeight / imageHeight
                     )
                     
                     // Step 3: Compute offsets (centering the fitted image)
                     val offsetX = (canvasWidth - imageWidth * scale) / 2f
                     val offsetY = (canvasHeight - imageHeight * scale) / 2f

                     Canvas(
                         modifier = Modifier
                             .fillMaxSize()
                             .pointerInput(Unit) {
                                 detectTapGestures { tapOffset ->
                                     // If we already have a selection, tapping elsewhere might deselect or do nothing
                                     // If NO selection, tap selects best face
                                     if (appState.state.selectedFace == null) {
                                         val tappedIndex = appState.state.detections.indexOfFirst { detection ->
                                             val left = detection.x1 * scale + offsetX
                                             val top = detection.y1 * scale + offsetY
                                             val width = (detection.x2 - detection.x1) * scale
                                             val height = (detection.y2 - detection.y1) * scale
                                             
                                             tapOffset.x >= left && tapOffset.x <= (left + width) &&
                                             tapOffset.y >= top && tapOffset.y <= (top + height)
                                         }
                                         
                                         if (tappedIndex != -1) {
                                             appState.selectFace(tappedIndex)
                                         }
                                     } else {
                                         // Face ALREADY selected, handle Eye Taps
                                         val selectedFaceId = appState.state.selectedFace!!
                                         val detection = appState.state.detections[selectedFaceId]
                                         val faceWidth = detection.x2 - detection.x1
                                         val eyeBoxSize = faceWidth * 0.15f
                                         val eyeBoxSizeHalf = eyeBoxSize / 2f
                                         
                                         fun checkTap(cx: Float?, cy: Float?): Boolean {
                                             if (cx == null || cy == null) return false
                                             val ex1 = cx - eyeBoxSizeHalf
                                             val ey1 = cy - eyeBoxSizeHalf
                                             val ex2 = cx + eyeBoxSizeHalf
                                             
                                             val screenLeft = ex1 * scale + offsetX
                                             val screenTop = ey1 * scale + offsetY
                                             val screenWidth = (ex2 - ex1) * scale
                                             // screenHeight = screenWidth (square)
                                             
                                             return tapOffset.x >= screenLeft && tapOffset.x <= (screenLeft + screenWidth) &&
                                                    tapOffset.y >= screenTop && tapOffset.y <= (screenTop + screenWidth)
                                         }

                                         if (checkTap(detection.leftEyeX, detection.leftEyeY)) {
                                             appState.selectEye(EyeType.Left)
                                         } else if (checkTap(detection.rightEyeX, detection.rightEyeY)) {
                                             appState.selectEye(EyeType.Right)
                                         }
                                     }
                                 }
                             }
                     ) {
                         appState.state.detections.forEachIndexed { index, detection ->
                             // Skip drawing other faces if one is selected? OR dim them?
                             // Prompt: "Highlight selected face"
                             val isSelected = appState.state.selectedFace == index
                             val isAnySelected = appState.state.selectedFace != null
                             
                             if (isAnySelected && !isSelected) {
                                 // Skip others or draw dim
                                 return@forEachIndexed 
                             }

                             // Step 4: Map each box from image space to screen space
                             val left = detection.x1 * scale + offsetX
                             val top = detection.y1 * scale + offsetY
                             val width = (detection.x2 - detection.x1) * scale
                             val height = (detection.y2 - detection.y1) * scale
                             
                             val color = if (isSelected) SuccessGreen else AccentOrange
                             val strokeWidth = if (isSelected) 5.dp.toPx() else 3.dp.toPx()

                             drawRect(
                                 color = color,
                                 topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                 size = androidx.compose.ui.geometry.Size(width, height),
                                 style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                             )
                             
                             if (isSelected) {
                                 // Draw label
                                 drawRect(
                                     color = color,
                                     topLeft = androidx.compose.ui.geometry.Offset(left, top - 40f),
                                     size = androidx.compose.ui.geometry.Size(width, 40f)
                                 )
                                 
                                 // Step 8: Eye Detection & Selection
                                 val faceWidth = detection.x2 - detection.x1
                                 val eyeBoxSize = faceWidth * 0.15f
                                 val eyeBoxSizeHalf = eyeBoxSize / 2f
                                 
                                 // Helper to draw eye box
                                 fun drawEyeBox(cx: Float?, cy: Float?, eyeType: EyeType) {
                                     if (cx != null && cy != null) {
                                         // Bounding box in image space
                                         val ex1 = cx - eyeBoxSizeHalf
                                         val ey1 = cy - eyeBoxSizeHalf
                                         val ex2 = cx + eyeBoxSizeHalf
                                         val ey2 = cy + eyeBoxSizeHalf // unused for drawing but logic wise
                                         
                                         // Map to screen space
                                         val screenLeft = ex1 * scale + offsetX
                                         val screenTop = ey1 * scale + offsetY
                                         val screenWidth = (ex2 - ex1) * scale
                                         val screenHeight = screenWidth // Square box
                                         
                                         val isEyeSelected = appState.state.selectedEye == eyeType
                                         val eyeColor = if (isEyeSelected) SuccessGreen else AccentOrange
                                         val eyeStroke = if (isEyeSelected) 4.dp.toPx() else 2.dp.toPx()
                                         
                                         drawRect(
                                             color = eyeColor,
                                             topLeft = androidx.compose.ui.geometry.Offset(screenLeft, screenTop),
                                             size = androidx.compose.ui.geometry.Size(screenWidth, screenHeight),
                                             style = if (isEyeSelected) androidx.compose.ui.graphics.drawscope.Fill else androidx.compose.ui.graphics.drawscope.Stroke(width = eyeStroke),
                                             alpha = if (isEyeSelected) 0.5f else 1.0f
                                         )
                                         
                                         if (!isEyeSelected) {
                                              drawRect(
                                                 color = eyeColor,
                                                 topLeft = androidx.compose.ui.geometry.Offset(screenLeft, screenTop),
                                                 size = androidx.compose.ui.geometry.Size(screenWidth, screenHeight),
                                                 style = androidx.compose.ui.graphics.drawscope.Stroke(width = eyeStroke)
                                             )
                                         }
                                     }
                                 }
                                 
                                 drawEyeBox(detection.leftEyeX, detection.leftEyeY, EyeType.Left)
                                 drawEyeBox(detection.rightEyeX, detection.rightEyeY, EyeType.Right)
                             }
                         }
                     }
                 }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // If a face is selected, we might hide the guides
                if (appState.state.selectedFace == null) {
                   // ... existing overlay ...
                   Box(
                        modifier = Modifier
                            .size(280.dp)
                            .border(3.dp, AccentOrange, shape = RoundedCornerShape(16.dp))
                    ) { 
                        // ... existing bracket code ...
                        // Corner brackets
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(AccentOrange, RoundedCornerShape(2.dp))
                                .align(Alignment.TopStart)
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(AccentOrange, RoundedCornerShape(2.dp))
                                .align(Alignment.TopEnd)
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(AccentOrange, RoundedCornerShape(2.dp))
                                .align(Alignment.BottomStart)
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(AccentOrange, RoundedCornerShape(2.dp))
                                .align(Alignment.BottomEnd)
                        )
                        
                        // Center crosshair
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(AccentOrange, shape = CircleShape)
                                .align(Alignment.Center)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Tap a face to select it",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                     // Instructions for selected state
                     Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 100.dp),
                        contentAlignment = Alignment.BottomCenter
                     ) {
                         Text(
                             if (appState.state.selectedEye != null) 
                                "Eye Selected: ${appState.state.selectedEye?.name}. Confirm?"
                             else 
                                "Face Selected. Tap Left or Right eye to select.",
                             color = SuccessGreen,
                             fontSize = 16.sp,
                             fontWeight = FontWeight.Bold,
                             modifier = Modifier
                                 .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                 .padding(8.dp)
                         )
                     }
                }
            }
            
            // Processing indicator
            if (isProcessing) {
                CircularProgressIndicator(color = AccentOrange)
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appState.state.selectedFace == null) {
                // Balance spacer to keep capture button centered
                Spacer(modifier = Modifier.size(48.dp))
                
                // Capture Button -> Changed to Next Button if selected
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            if (isProcessing) Color.Gray else AccentOrange,
                            shape = CircleShape
                        )
                        .clickable(enabled = false) { // Disable manual capture for now, using tap selection workflow
                             // Workflow changed: Tap face -> Select -> Then confirm
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lens,
                        contentDescription = "Capture",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Camera Switch Button
                IconButton(
                    onClick = { 
                        cameraLens = if (cameraLens == CameraLens.Back) CameraLens.Front else CameraLens.Back 
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            } else {
                // Reset Button
                 IconButton(
                    onClick = { appState.selectFace(null) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Reset",
                        tint = Color.White
                    )
                }
                
                // Confirm selection (Proceed to next step)
                PrimaryButton(
                    text = "Confirm Eye",
                    onClick = { 
                        // Logic for next step (Eye Detection/Result)
                        // For now we just trigger capture to simulate "Done"
                        // In real flow, we would probably just navigate to result with the cropped eye
                        onCapture(androidx.compose.ui.graphics.ImageBitmap(1,1)) 
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    enabled = appState.state.selectedEye != null
                )
            }
        }
    }
}

@Composable
fun FaceDetectionScreen(
    capturedImage: androidx.compose.ui.graphics.ImageBitmap?,
    selectedFace: Int?,
    onBack: () -> Unit,
    onFaceSelected: (Int) -> Unit,
    detections: List<com.org.humanfaceeyedetector.state.DetectionResult> = emptyList(),
    isProcessing: Boolean = false
) {
    var localSelectedFaceIndex by remember { mutableStateOf(selectedFace) }
    var imageSize by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    PlatformBackHandler {
        onBack()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(
            title = "Select Face",
            onBackClick = onBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (localSelectedFaceIndex != null && detections.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    color = CardBackground,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val selectedDetection = detections.getOrNull(localSelectedFaceIndex!!)
                        val confidence = selectedDetection?.confidence?.let { (it * 100).toInt() } ?: 0
                        Text(
                            "Face ${localSelectedFaceIndex!! + 1} selected (${confidence}% confidence)",
                            color = AccentOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Status text
            val statusText = when {
                isProcessing -> "Processing detections…"
                detections.isEmpty() -> "No faces detected"
                else -> "${detections.size} face${if (detections.size != 1) "s" else ""} detected — tap to select"
            }
            
            Text(
                statusText,
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
            
            // Display captured image with bounding boxes (Step-6)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(CardBackground, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImage != null && !isProcessing) {
                    BoundingBoxOverlay(
                        image = capturedImage,
                        detections = detections,
                        selectedFaceIndex = localSelectedFaceIndex,
                        onFaceTapped = { faceIndex ->
                            localSelectedFaceIndex = faceIndex
                        },
                        onImageSizeKnown = { width, height ->
                            imageSize = width to height
                        }
                    )
                } else if (isProcessing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(color = AccentOrange)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Detecting faces…", color = TextSecondary)
                    }
                } else {
                    Text("No image captured", color = TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryButton(
                text = "Confirm Face Selection",
                onClick = {
                    if (localSelectedFaceIndex != null) {
                        onFaceSelected(localSelectedFaceIndex!!)
                    }
                },
                enabled = localSelectedFaceIndex != null && !isProcessing
            )
            
            SecondaryButton(
                text = "Back to Home",
                onClick = onBack
            )
        }
    }
}

/**
 * Step-6: Displays image with bounding box overlays
 * Allows user to tap faces to select them
 */
@Composable
fun BoundingBoxOverlay(
    image: androidx.compose.ui.graphics.ImageBitmap,
    detections: List<com.org.humanfaceeyedetector.state.DetectionResult>,
    selectedFaceIndex: Int?,
    onFaceTapped: (Int) -> Unit,
    onImageSizeKnown: (Int, Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        val density = LocalDensity.current
        
        // Get container size in pixels
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        
        // Calculate image scaling to fit container (ContentScale.Fit)
        val imageWidth = image.width.toFloat()
        val imageHeight = image.height.toFloat()
        
        val scale = minOf(containerWidthPx / imageWidth, containerHeightPx / imageHeight)
        
        // Calculate centered image offset
        val offsetX = (containerWidthPx - imageWidth * scale) / 2
        val offsetY = (containerHeightPx - imageHeight * scale) / 2
        
        // Display image
        Image(
            bitmap = image,
            contentDescription = "Captured image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        
        // Draw bounding boxes with Canvas
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Notify parent of display size (using actual image size)
            onImageSizeKnown(imageWidth.toInt(), imageHeight.toInt())
            
            // Draw each detection
            detections.forEachIndexed { index, detection ->
                val isSelected = selectedFaceIndex == index
                
                // Calculate box position and size
                // Coordinate transformation: model/image space -> screen space
                val left = detection.x1 * scale + offsetX
                val top = detection.y1 * scale + offsetY
                val right = detection.x2 * scale + offsetX
                val bottom = detection.y2 * scale + offsetY
                
                val width = right - left
                val height = bottom - top
                
                // Draw selection highlight if selected
                if (isSelected) {
                    drawRect(
                        color = AccentOrange.copy(alpha = 0.1f),
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(width, height)
                    )
                }
                
                // Draw bounding box
                drawRect(
                    color = AccentOrange,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(width, height),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
                
                // Draw label background
                val labelText = "Face ${index + 1}\n${(detection.confidence * 100).toInt()}%"
                drawRect(
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f),
                    topLeft = androidx.compose.ui.geometry.Offset(left, top - 30f),
                    size = androidx.compose.ui.geometry.Size(width, 30f)
                )
            }
        }
        
        // Clickable areas for each detection
        detections.forEachIndexed { index, detection ->
            val leftPx = detection.x1 * scale + offsetX
            val topPx = detection.y1 * scale + offsetY
            val widthPx = (detection.x2 - detection.x1) * scale
            val heightPx = (detection.y2 - detection.y1) * scale
            
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { leftPx.toDp() },
                        y = with(density) { topPx.toDp() }
                    )
                    .size(
                        width = with(density) { widthPx.toDp() },
                        height = with(density) { heightPx.toDp() }
                    )
                    .clickable { onFaceTapped(index) }
            )
        }
    }
}

@Composable
fun EyeDetectionScreen(
    capturedImage: androidx.compose.ui.graphics.ImageBitmap?,
    selectedFace: Int?,
    selectedEye: EyeType?,
    eyeDetections: List<com.org.humanfaceeyedetector.state.DetectionResult> = emptyList(),
    isProcessing: Boolean = false,
    onBack: () -> Unit,
    onEyeSelected: (EyeType) -> Unit
) {
    var localSelectedEye by remember { mutableStateOf(selectedEye) }
    var imageSize by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    // Map eye detections to left/right: index 0 = left, index 1 = right
    val leftEyeBox = eyeDetections.find { it.faceId == 0 }
    val rightEyeBox = eyeDetections.find { it.faceId == 1 }
    
    PlatformBackHandler {
        onBack()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(
            title = "Select Eye",
            onBackClick = onBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (localSelectedEye != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    color = CardBackground,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val eyeName = if (localSelectedEye == EyeType.Left) leftEyeBox else rightEyeBox
                        val confidence = eyeName?.confidence?.let { (it * 100).toInt() } ?: 0
                        Text(
                            "${localSelectedEye!!.name} Eye selected (${confidence}% confidence)",
                            color = AccentOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Status text
            val statusText = when {
                isProcessing -> "Detecting eyes…"
                eyeDetections.isEmpty() -> "No eyes detected"
                else -> "${eyeDetections.size} eye${if (eyeDetections.size != 1) "s" else ""} detected — tap to select"
            }
            
            Text(
                statusText,
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
            
            // Display captured image with eye bounding boxes (Step-8)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(CardBackground, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImage != null && !isProcessing) {
                    BoundingBoxOverlay(
                        image = capturedImage,
                        detections = eyeDetections,
                        selectedFaceIndex = localSelectedEye?.ordinal, // 0=Left, 1=Right
                        onFaceTapped = { eyeIndex ->
                            val eyeType = if (eyeIndex == 0) EyeType.Left else EyeType.Right
                            localSelectedEye = eyeType
                        },
                        onImageSizeKnown = { width, height ->
                            imageSize = width to height
                        }
                    )
                } else if (isProcessing) {
                     Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(color = AccentOrange)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Detecting eyes…", color = TextSecondary)
                    }
                } else {
                    Text("No image available", color = TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryButton(
                text = "Confirm Eye Selection",
                onClick = { 
                    if (localSelectedEye != null) {
                        onEyeSelected(localSelectedEye!!) 
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = localSelectedEye != null && !isProcessing
            )
        }
    }
}

@Composable
fun ResultScreen(
    capturedImage: androidx.compose.ui.graphics.ImageBitmap?,
    selectedFace: Int?,
    selectedEye: EyeType?,
    captureTimestamp: Long? = null,
    detections: List<com.org.humanfaceeyedetector.state.DetectionResult> = emptyList(),
    eyeDetections: List<com.org.humanfaceeyedetector.state.DetectionResult> = emptyList(),
    onBack: () -> Unit,
    onNewScan: () -> Unit
) {
    PlatformBackHandler {
        onBack()
    }
    
    // Get selected face and eye bounding boxes
    val selectedFaceBox = if (selectedFace != null && selectedFace < detections.size) {
        detections[selectedFace]
    } else {
        null
    }
    
    val selectedEyeBox = if (selectedEye != null && 
        selectedFace != null && 
        selectedFace < detections.size) {
        val eyeIndex = when (selectedEye) {
            EyeType.Left -> 0
            EyeType.Right -> 1
        }
        eyeDetections.getOrNull(eyeIndex)
    } else {
        null
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(
            title = "Detection Result",
            onBackClick = onBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                color = SuccessGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✓ Detection Complete",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Image with bounding boxes (Step-8)
            if (capturedImage != null && selectedFaceBox != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(CardBackground, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ResultImageWithBoxes(
                        image = capturedImage,
                        faceBox = selectedFaceBox,
                        eyeBox = selectedEyeBox,
                        selectedEye = selectedEye
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(CardBackground, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No image available", color = TextSecondary)
                }
            }
            
            // Metadata panel
            InfoCard(title = "Detection Details") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Format timestamp with locale awareness
                    val captureTimeStr = if (captureTimestamp != null) {
                        formatTimestamp(captureTimestamp)
                    } else {
                        "-"
                    }
                    
                    DetectionDetailRow("Captured", captureTimeStr)
                    DetectionDetailRow("Face ID", "${selectedFace?.plus(1) ?: "?"}")
                    DetectionDetailRow("Selected Eye", selectedEye?.name ?: "-")
                    
                    // Show actual confidence
                    val eyeConfidence = selectedEyeBox?.confidence?.let { (it * 100).toInt() } ?: 0
                    DetectionDetailRow("Eye Confidence", "$eyeConfidence%")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ConfidenceBar(confidence = selectedEyeBox?.confidence ?: 0f)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PrimaryButton(
                text = "Start New Scan",
                onClick = onNewScan
            )
            
            SecondaryButton(
                text = "Back",
                onClick = onBack
            )
        }
    }
}

/**
 * Step-8: Display image with face and eye bounding boxes
 */
@Composable
fun ResultImageWithBoxes(
    image: androidx.compose.ui.graphics.ImageBitmap,
    faceBox: com.org.humanfaceeyedetector.state.DetectionResult,
    eyeBox: com.org.humanfaceeyedetector.state.DetectionResult?,
    selectedEye: EyeType?
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        val density = LocalDensity.current
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        
        // Calculate image scaling to fit container
        val imageWidth = image.width.toFloat()
        val imageHeight = image.height.toFloat()
        
        val scale = minOf(containerWidthPx / imageWidth, containerHeightPx / imageHeight)
        val offsetX = (containerWidthPx - imageWidth * scale) / 2
        val offsetY = (containerHeightPx - imageHeight * scale) / 2
        
        // Display image
        Image(
            bitmap = image,
            contentDescription = "Result image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        
        // Draw bounding boxes
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw face box (thin orange line)
            val faceLeft = faceBox.x1 * scale + offsetX
            val faceTop = faceBox.y1 * scale + offsetY
            val faceRight = faceBox.x2 * scale + offsetX
            val faceBottom = faceBox.y2 * scale + offsetY
            val faceWidth = faceRight - faceLeft
            val faceHeight = faceBottom - faceTop
            
            drawRect(
                color = AccentOrange,
                topLeft = androidx.compose.ui.geometry.Offset(faceLeft, faceTop),
                size = androidx.compose.ui.geometry.Size(faceWidth, faceHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            
            // Draw eye box (highlighted green)
            if (eyeBox != null) {
                val eyeLeft = eyeBox.x1 * scale + offsetX
                val eyeTop = eyeBox.y1 * scale + offsetY
                val eyeRight = eyeBox.x2 * scale + offsetX
                val eyeBottom = eyeBox.y2 * scale + offsetY
                val eyeWidth = eyeRight - eyeLeft
                val eyeHeight = eyeBottom - eyeTop
                
                // Highlight background
                drawRect(
                    color = SuccessGreen.copy(alpha = 0.15f),
                    topLeft = androidx.compose.ui.geometry.Offset(eyeLeft, eyeTop),
                    size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight)
                )
                
                // Border
                drawRect(
                    color = SuccessGreen,
                    topLeft = androidx.compose.ui.geometry.Offset(eyeLeft, eyeTop),
                    size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
                
                // Label
                drawRect(
                    color = SuccessGreen,
                    topLeft = androidx.compose.ui.geometry.Offset(eyeLeft, eyeTop - 25f),
                    size = androidx.compose.ui.geometry.Size(eyeWidth, 25f)
                )
            }
        }
    }
}
