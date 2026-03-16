# Model Output Specification

The model returns a single tensor with the shape `[1, 300, 6]`.

- **Output Tensor Shape**: `[1, 300, 6]`
- **Description**: The tensor contains the top 300 detections after Non-Maximal Suppression (NMS). Each detection has 6 values.

## Detection Attributes

The 6 values for each detection are structured as follows:

- `[x1, y1, x2, y2, confidence, class_id]`

Where:
- **`x1`, `y1`**: Top-left coordinates of the bounding box.
- **`x2`, `y2`**: Bottom-right coordinates of the bounding box.
- **`confidence`**: The probability score of the detection (a `float32` value between 0.0 and 1.0).
- **`class_id`**: The integer ID of the detected class.

## Bounding Box Format

The bounding boxes are provided in absolute pixel coordinates corresponding to the original 640x640 input image size. They need to be scaled back to the original image's dimensions before display.

## Post-processing

- **Confidence Threshold**: A confidence threshold should be applied to filter out low-probability detections. A typical starting value is `0.45`.
- **NMS**: NMS has already been applied during the export process, so no further NMS is required on the client-side.
