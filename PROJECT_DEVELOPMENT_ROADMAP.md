CHAKRAVUE EYE DETECTOR
PROJECT DEVELOPMENT ROADMAP (REVISED ARCHITECTURE)

Package:
com.org.humanfaceeyedetector

Framework:
Kotlin Multiplatform
Compose Multiplatform
CameraX
ML Kit Vision (Face Detection)

---

PROJECT PURPOSE

The application captures an image of a human face and detects eye regions using on-device ML.

Users will:

1 Capture or upload an image
2 Detect multiple faces
3 Select a face
4 Select left or right eye
5 View detection result

The application runs entirely on-device with no server dependency.

---

SYSTEM ARCHITECTURE

commonMain
Platform independent code

Contains:
UI components
navigation
application state

androidMain
Android-specific logic

Contains:
CameraX implementation
ML Kit Face Detection
Eye landmark processing
Image preprocessing
Bounding box rendering

iosMain
Minimal entry setup (ML not enabled initially)

---

ML MODEL DESIGN

IMPORTANT CHANGE:

YOLOv8 is NO LONGER used for face detection.

Face detection is handled by:

ML Kit Face Detection API

Capabilities:

Detect multiple faces
Provide face bounding boxes
Provide facial landmarks (eyes)

---

APPLICATION FLOW

Splash Screen

Initialize ML Kit Face Detector

↓

Home Screen

Capture Image
Upload Image
Live Camera

↓

Camera Screen

Preview camera
Capture image

↓

Face Detection Stage (CRITICAL)

Run ML Kit Face Detection

Output:
List of face bounding boxes

↓

Face Selection Screen

Display all detected faces
User selects one face

↓

Eye Detection Stage

Crop selected face
Extract:

Left eye landmark
Right eye landmark

Convert landmarks → bounding boxes

↓

Eye Selection Screen

Display both eyes
User selects one

↓

Result Screen

Display:

Selected face
Selected eye
Bounding box
Confidence (if applicable)

---

PROJECT DEVELOPMENT STEPS (UPDATED)

STEP 0
PREVIOUS STEPS 1-5 UI, NAVIGATION, CAMERA, AND USERFLOW are complete and functional.

STEP 1
Environment setup
ML Kit dependency integration

STEP 2
Compose UI implementation

STEP 3
Navigation system

STEP 4
Camera integration (CameraX)

STEP 5
Face Detection (ML Kit)

Detect faces
Return bounding boxes

STEP 6
Bounding box rendering

Draw face boxes
Enable selection

STEP 7
Face selection logic

User selects face
Persist selection

STEP 8
Eye detection

Extract landmarks
Generate eye bounding boxes

STEP 9
Result rendering

Display selected eye

STEP 10
Performance optimization

Threading
Bitmap reuse
Reduce allocations

---

ENGINEERING PRINCIPLES

DO NOT use object detection models for face detection

Always separate:

Face detection
Eye detection

Never assume geometry (no heuristic eye positions)

Use landmark-based detection for accuracy

Maintain single pipeline flow

Do not duplicate NMS or filtering if already handled

Ensure correct coordinate scaling

---

FINAL ARCHITECTURE SUMMARY

Camera → Face Detection → Face Selection → Eye Detection → Result

NOT:

Camera → YOLO → Guess → Pray

---

END OF ROADMAP
