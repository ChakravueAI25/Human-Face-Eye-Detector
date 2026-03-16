
import cv2
import numpy as np
import tensorflow as tf
import json
from preprocessing_logic import preprocess
from postprocessing_logic import postprocess

# Load the TFLite model and allocate tensors.
interpreter = tf.lite.Interpreter(model_path="models/model_fp32.tflite")
interpreter.allocate_tensors()

# Get input and output tensors.
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Preprocess the image
input_data = preprocess("sample.jpg")

# Set the tensor
interpreter.set_tensor(input_details[0]['index'], input_data)

# Run inference
interpreter.invoke()

# Get the output
output_data = interpreter.get_tensor(output_details[0]['index'])

# Postprocess the output
boxes, scores, class_ids = postprocess(output_data)

# Create expected_output.json
output_json = []
for box, score, class_id in zip(boxes, scores, class_ids):
    output_json.append({
        "box": [int(b) for b in box],
        "score": float(score),
        "class_id": int(class_id)
    })

with open("expected_output.json", "w") as f:
    json.dump(output_json, f, indent=4)

# Draw the bounding boxes on the image
image = cv2.imread("sample.jpg")
for box, score, class_id in zip(boxes, scores, class_ids):
    x, y, w, h = box
    cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)
    label = f"Class {class_id}: {score:.2f}"
    cv2.putText(image, label, (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

cv2.imwrite("output_example.jpg", image)

print("Inference complete. 'expected_output.json' and 'output_example.jpg' created.")
