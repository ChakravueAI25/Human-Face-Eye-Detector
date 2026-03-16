Global UI Theme

Color tokens used across all screens:

Background        #0F1115
Surface           #1C1F26
Card              #242833
Primary Accent    #F4A259
Secondary Accent  #FFB86C
Text Primary      #E6E6E6
Text Secondary    #9AA0A6

Material specs:

Corner radius   16dp
Button height   52dp
Spacing grid    8dp
Font            Roboto
Frame           360x800
Screen 1 — Splash

Layout

[ Background #0F1115 ]

        Logo

ChakraVue Eye Scanner

Initializing AI Detection Engine

[ Circular Progress Indicator ]

Navigation

Auto → Home Screen
Screen 2 — Home

Top App Bar

Eye Detection

Main content (vertical buttons)

[ Capture Image ]
[ Upload Image ]
[ Live Camera Detection ]

Button style

Material Filled Button
Color: #F4A259
Height: 52dp
Radius: 16dp

Bottom model info card

Card (#242833)

Model: YOLOv8n
Input: 640x640

Navigation

Capture Image → Camera Screen
Upload Image → Gallery
Live Camera → Camera Screen
Screen 3 — Face Detection

Top bar

← Select Face

Main image area

[ Image Preview ]

 ┌───────────────┐
 │   Face 1      │
 └───────────────┘

 ┌───────────────┐
 │   Face 2      │
 └───────────────┘

Bounding box

Rounded rectangle
Stroke: #F4A259
Label background: #000000AA

Interaction

Tap Face → Highlight box

Bottom action area

[ Confirm Face Selection ]
[ Back to Home ]

Navigation

Confirm → Eye Detection
Back → Home
Screen 4 — Eye Detection

Top bar

← Select Eye

Main content

Zoomed Face Image

Eye overlays

 ┌─────────┐
 │ Left Eye│
 └─────────┘

 ┌─────────┐
 │Right Eye│
 └─────────┘

Selection behavior

Tap → highlight eye

Bottom action panel

[ Confirm Eye ]
[ Reselect Face ]

Navigation

Confirm → Result Screen
Reselect → Face Selection
Screen 5 — Result Screen

Top bar

Detection Result

Main preview

Image
Highlighted Eye Bounding Box

Info card

Card (#242833)

Selected Face ID
Selected Eye
Confidence Score

Action buttons

[ Save Result ]
[ Start New Scan ]
[ Back ]

Navigation

Start New Scan → Home
Back → Eye Selection
Touch Interaction Requirement

Bounding boxes must include expanded tap area:

12dp outside bounding box

Otherwise users miss taps constantly.

Component Library (for Figma)

Create these reusable components:

PrimaryActionButton
FaceBoundingBox
EyeBoundingBox
DetectionCard
ResultInfoCard
Prototype Navigation
Splash
  ↓
Home
  ↓
Capture / Upload
  ↓
Face Selection
  ↓
Eye Selection
  ↓
Result

Back navigation works at every step.