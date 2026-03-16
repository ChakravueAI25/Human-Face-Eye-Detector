package com.org.humanfaceeyedetector.ui

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
import com.org.humanfaceeyedetector.camera.CameraPreview
import com.org.humanfaceeyedetector.camera.RequestCaptureImage
import com.org.humanfaceeyedetector.navigation.AppNavigation
import com.org.humanfaceeyedetector.platform.PlatformBackHandler
import com.org.humanfaceeyedetector.state.EyeType
import com.org.humanfaceeyedetector.state.rememberAppState
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lens
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
    onBack: () -> Unit,
    onCapture: (imageBitmap: androidx.compose.ui.graphics.ImageBitmap) -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var shouldCapture by remember { mutableStateOf(false) }
    
    PlatformBackHandler {
        onBack()
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
            title = "Camera Capture",
            onBackClick = onBack
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
                modifier = Modifier
                    .fillMaxSize()
            )
            
            // Overlay UI (top layer)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .border(3.dp, AccentOrange, shape = RoundedCornerShape(16.dp))
                ) {
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
                    
                    // Center scanning line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(AccentOrange)
                            .align(Alignment.Center)
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
                    "Position face within the frame",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
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
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        if (isProcessing) Color.Gray else AccentOrange,
                        shape = CircleShape
                    )
                    .clickable(enabled = !isProcessing) {
                        isProcessing = true
                        shouldCapture = true
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
    selectedFace: Int?,
    selectedEye: EyeType?,
    eyeDetections: List<com.org.humanfaceeyedetector.state.DetectionResult> = emptyList(),
    isProcessing: Boolean = false,
    onBack: () -> Unit,
    onEyeSelected: (EyeType) -> Unit
) {
    var localSelectedEye by remember { mutableStateOf(selectedEye) }
    
    // Map eye detections to left/right: index 0 = left, index 1 = right
    val leftEyeBox = eyeDetections.getOrNull(0)
    val rightEyeBox = eyeDetections.getOrNull(1)
    
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
            
            // Display face with eye detections
            if (eyeDetections.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(CardBackground, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isProcessing) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                "Face ${selectedFace?.plus(1) ?: "?"} – Eye Options",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Left Eye
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .border(
                                            width = if (localSelectedEye == EyeType.Left) 3.dp else 2.dp,
                                            color = if (localSelectedEye == EyeType.Left) SuccessGreen else AccentOrange,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { localSelectedEye = EyeType.Left },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("Left Eye", color = TextPrimary, fontSize = 12.sp)
                                        if (leftEyeBox != null) {
                                            Text(
                                                "${(leftEyeBox.confidence * 100).toInt()}%",
                                                color = AccentOrange,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                                
                                // Right Eye
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .border(
                                            width = if (localSelectedEye == EyeType.Right) 3.dp else 2.dp,
                                            color = if (localSelectedEye == EyeType.Right) SuccessGreen else AccentOrange,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { localSelectedEye = EyeType.Right },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("Right Eye", color = TextPrimary, fontSize = 12.sp)
                                        if (rightEyeBox != null) {
                                            Text(
                                                "${(rightEyeBox.confidence * 100).toInt()}%",
                                                color = AccentOrange,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator(color = AccentOrange)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Detecting eyes…", color = TextSecondary)
                        }
                    }
                }
            } else if (!isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(CardBackground, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("No eyes detected", color = TextSecondary)
                        Text("Please select a different face", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(CardBackground, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(color = AccentOrange)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Processing…", color = TextSecondary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryButton(
                text = "Confirm Eye",
                onClick = {
                    if (localSelectedEye != null) {
                        onEyeSelected(localSelectedEye!!)
                    }
                },
                enabled = localSelectedEye != null && !isProcessing && eyeDetections.isNotEmpty()
            )
            
            SecondaryButton(
                text = "Reselect Face",
                onClick = onBack
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
                        val formatter = java.text.SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                            java.util.Locale.getDefault()
                        )
                        formatter.format(java.util.Date(captureTimestamp))
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
