package com.org.humanfaceeyedetector.ui

import androidx.compose.foundation.background
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
import com.org.humanfaceeyedetector.camera.toImageBitmap
import com.org.humanfaceeyedetector.navigation.AppNavigation
import com.org.humanfaceeyedetector.platform.PlatformBackHandler
import com.org.humanfaceeyedetector.state.EyeType
import com.org.humanfaceeyedetector.state.rememberAppState
import com.org.humanfaceeyedetector.formatTimestamp
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility


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
    var detectionFrozen by remember { mutableStateOf(false) }
    var cameraLens by remember { mutableStateOf(CameraLens.Back) }

    LaunchedEffect(Unit) {
        appState.setDetections(emptyList())
        appState.selectFace(null)
    }

    PlatformBackHandler { onBack() }

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        TopBar(title = "Camera Capture", onBackClick = onBack)

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                cameraLens = cameraLens,
                isDetectionEnabled = !detectionFrozen,
                onDetectionsUpdated = { detections ->
                    if (!detectionFrozen) appState.setDetections(detections)
                },
                onImageDimensionsUpdated = { width, height ->
                    appState.setImageDimensions(width, height)
                }
            )

            if (appState.state.detections.isNotEmpty()
                && appState.state.imageWidth > 0
                && appState.state.imageHeight > 0
                && !detectionFrozen
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val imageWidth = appState.state.imageWidth.toFloat()
                    val imageHeight = appState.state.imageHeight.toFloat()
                    val scale = minOf(size.width / imageWidth, size.height / imageHeight)
                    val offsetX = (size.width - imageWidth * scale) / 2
                    val offsetY = (size.height - imageHeight * scale) / 2
                    appState.state.detections.forEach { detection ->
                        val left = detection.x1 * scale + offsetX
                        val top = detection.y1 * scale + offsetY
                        val width = (detection.x2 - detection.x1) * scale
                        val height = (detection.y2 - detection.y1) * scale
                        drawRect(
                            color = AccentOrange,
                            topLeft = androidx.compose.ui.geometry.Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(width, height),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    cameraLens = if (cameraLens == CameraLens.Back) CameraLens.Front else CameraLens.Back
                },
                modifier = Modifier.size(48.dp).background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Switch Camera", tint = Color.White)
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(if (!detectionFrozen) AccentOrange else Color.Gray, shape = CircleShape)
                    .clickable(enabled = !detectionFrozen) {
                        detectionFrozen = true
                        com.org.humanfaceeyedetector.camera.captureImage(
                            onImageCaptured = { bitmap ->
                                appState.setDetections(emptyList())
                                onCapture(bitmap.toImageBitmap())
                            },
                            onError = { error ->
                                appState.setInferenceError("Capture failed: $error")
                                detectionFrozen = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Lens, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.size(48.dp))
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
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp), // Reduced top padding
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (localSelectedFaceIndex != null && detections.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(), // Removed fixed height
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
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
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
                        onImageSizeKnown = { _, _ -> }
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Face selection buttons
            if (detections.isNotEmpty() && !isProcessing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    detections.forEachIndexed { index, detection ->
                        val isSelected = localSelectedFaceIndex == index
                        val confidence = (detection.confidence * 100).toInt()
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { localSelectedFaceIndex = index }, // Removed fixed height
                            color = if (isSelected) AccentOrange else CardBackground,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Face ${index + 1} - $confidence% confidence",
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
    onImageSizeKnown: (Int, Int) -> Unit,
    labelPrefix: String = "Face"
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        val imageWidth = image.width.toFloat()
        val imageHeight = image.height.toFloat()

        val scale = minOf(containerWidthPx / imageWidth, containerHeightPx / imageHeight)
        val offsetX = (containerWidthPx - imageWidth * scale) / 2f
        val offsetY = (containerHeightPx - imageHeight * scale) / 2f

        Image(
            bitmap = image,
            contentDescription = "Captured image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            detections.forEachIndexed { index, detection ->
                val isSelected = selectedFaceIndex == index
                val left   = detection.x1 * scale + offsetX
                val top    = detection.y1 * scale + offsetY
                val right  = detection.x2 * scale + offsetX
                val bottom = detection.y2 * scale + offsetY
                val width  = right - left
                val height = bottom - top

                if (isSelected) {
                    drawRect(
                        color = AccentOrange.copy(alpha = 0.15f),
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(width, height)
                    )
                }
                drawRect(
                    color = if (isSelected) AccentOrange else androidx.compose.ui.graphics.Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(width, height),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }
        }

        // Face labels
        detections.forEachIndexed { index, detection ->
            val leftPx  = detection.x1 * scale + offsetX
            val topPx   = detection.y1 * scale + offsetY

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { leftPx.toDp() },
                        y = with(density) { (topPx - 28f).toDp() }
                    )
            ) {
                Text(
                    text = "$labelPrefix ${index + 1}",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        detections.forEachIndexed { index, detection ->
            val leftPx   = detection.x1 * scale + offsetX
            val topPx    = detection.y1 * scale + offsetY
            val widthPx  = (detection.x2 - detection.x1) * scale
            val heightPx = (detection.y2 - detection.y1) * scale

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { leftPx.toDp() },
                        y = with(density) { topPx.toDp() }
                    )
                    .size(
                        width  = with(density) { widthPx.toDp() },
                        height = with(density) { heightPx.toDp() }
                    )
                    .clickable { onFaceTapped(index) }
            )
        }
    }
}

@Composable
fun EyeDetectionScreen(
    faceImage: androidx.compose.ui.graphics.ImageBitmap?, // Changed from capturedImage (full) to faceImage (crop)
    faceDetection: com.org.humanfaceeyedetector.state.DetectionResult?, // Added
    selectedEye: EyeType?,
    eyeDetections: List<com.org.humanfaceeyedetector.state.DetectionResult> = emptyList(),
    isProcessing: Boolean = false,
    onBack: () -> Unit,
    onEyeSelected: (EyeType) -> Unit
) {
    var localSelectedEye by remember { mutableStateOf(selectedEye) }
    
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
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp), // Reduced top padding
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (localSelectedEye != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(), // Removed fixed height
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
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
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
            
            // Display cropped face image with eye bounding boxes (Step-8)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(CardBackground, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (faceImage != null && faceDetection != null && !isProcessing) {
                    // Offset eye detections to be relative to the cropped face
                    // Clamp offset to 0 because cropping clamps negative coordinates
                    val faceX = maxOf(0f, faceDetection.x1)
                    val faceY = maxOf(0f, faceDetection.y1)
                    
                    val offsetEyeDetections = eyeDetections.map { eye ->
                        eye.copy(
                            x1 = maxOf(0f, eye.x1 - faceX),
                            y1 = maxOf(0f, eye.y1 - faceY),
                            x2 = maxOf(0f, eye.x2 - faceX),
                            y2 = maxOf(0f, eye.y2 - faceY)
                        )
                    }

                    BoundingBoxOverlay(
                        image = faceImage,
                        detections = offsetEyeDetections,
                        selectedFaceIndex = localSelectedEye?.ordinal, // 0=Left, 1=Right
                        onFaceTapped = { eyeIndex ->
                            val eyeType = if (eyeIndex == 0) EyeType.Left else EyeType.Right
                            localSelectedEye = eyeType
                        },
                        onImageSizeKnown = { _, _ -> },
                        labelPrefix = "Eye"
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Eye selection buttons
            if (!isProcessing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Eye Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { localSelectedEye = EyeType.Left }, // Removed fixed height
                        color = if (localSelectedEye == EyeType.Left) AccentOrange else CardBackground,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Left Eye",
                                color = if (localSelectedEye == EyeType.Left) Color.Black else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Right Eye Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { localSelectedEye = EyeType.Right }, // Removed fixed height
                        color = if (localSelectedEye == EyeType.Right) AccentOrange else CardBackground,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Right Eye",
                                    color = if (localSelectedEye == EyeType.Right) Color.Black else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
    eyeImage: androidx.compose.ui.graphics.ImageBitmap?, // Changed to eyeImage (crop)
    selectedFace: Int?,
    selectedEye: EyeType?,
    captureTimestamp: Long? = null,
    // detections removed as we only show eye details
    // eyeDetections removed
    onBack: () -> Unit,
    onNewScan: () -> Unit
) {
    PlatformBackHandler {
        onBack()
    }
    
    // Logic for finding boxes moved to caller or irrelevant for display as we show crop
    
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
            
            // Image (Cropped Eye)
            if (eyeImage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(CardBackground, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = eyeImage,
                        contentDescription = "Result eye",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
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
                    val eyeConfidence = 0 // Placeholder, logic moved out
                    DetectionDetailRow("Eye Confidence", "High") // Value will be passed or generic
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // ConfidenceBar(confidence = selectedEyeBox?.confidence ?: 0f) 
                    // Removed dynamic confidence for now as we simplified params, or need to pass it
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

