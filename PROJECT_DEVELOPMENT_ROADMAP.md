CHAKRAVUE EYE DETECTOR
PROJECT DEVELOPMENT ROADMAP

Package:
com.org.humanfeaceeyedetector

Framework:
Kotlin Multiplatform
Compose Multiplatform
TensorFlow Lite
CameraX

---

PROJECT PURPOSE

The application captures an image of a human face and detects eye regions using a YOLOv8 TensorFlow Lite model.

Users will:

1 Capture or upload an image
2 Select a detected face
3 Select left or right eye
4 View detection result

The application runs ML inference locally on the device.

---

SYSTEM ARCHITECTURE

commonMain
Platform independent code.

Contains:

UI components
navigation
application state

androidMain
Android specific logic.

Contains:

CameraX implementation
TensorFlow Lite inference
Image preprocessing
Bounding box processing

iosMain

Contains minimal entry code for iOS builds.

ML inference will not initially run on iOS.

---

ML MODEL

Model:
YOLOv8n converted to TensorFlow Lite

Input:

640 x 640 RGB image

Normalized pixel values:

pixel / 255

Tensor shape:

[1, 640, 640, 3]

Output tensor:

[1, 300, 6]

Each detection:

x1
y1
x2
y2
confidence
class_id

---

APPLICATION FLOW

Splash Screen

Loads ML model
Initializes detection engine

↓

Home Screen

User options:

Capture Image
Upload Image
Live Camera

↓

Camera Screen

Preview camera
Capture image

↓

Face Detection Screen

Display detected faces
User taps a face bounding box

↓

Eye Detection Screen

Zoom into selected face
Display left and right eye regions

User selects an eye

↓

Result Screen

Display selected eye bounding box
Show:

Face ID
Eye
Confidence

User can start a new scan.

---

PROJECT DEVELOPMENT STEPS

STEP 1
Environment setup
Dependencies
Model placement
File structure

STEP 2
Compose UI implementation

Screens:

Splash
Home
Camera
Face Selection
Eye Selection
Result

UI design is based on the Figma prototype in the Camera App Design folder.

STEP 3
Navigation system implementation

Compose Navigation
Screen state transitions

STEP 4
Camera integration

CameraX preview
Image capture
Bitmap conversion

STEP 5
ML inference pipeline

Image preprocessing
Tensor conversion
TFLite inference
Detection parsing

STEP 6
Bounding box overlay system

Map model coordinates
Render detection boxes
Enable selection interaction

STEP 7
Face selection logic

Filter detections
Allow user selection

STEP 8
Eye detection interaction

Crop face region
Display selectable eye regions

STEP 9
Result rendering

Overlay selected eye
Display metadata

STEP 10
Performance optimization

Switch to INT8 model if needed
Thread optimization
Reduce UI blocking

---

ENGINEERING PRINCIPLES

Keep directory structure minimal.

Separate platform-specific code.

Never block UI thread with ML inference.

Maintain clear navigation flow.

Ensure bounding boxes are correctly scaled from model space to image space.

---

END OF ROADMAP
