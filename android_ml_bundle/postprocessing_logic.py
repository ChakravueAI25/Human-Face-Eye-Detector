
import numpy as np
import cv2

def postprocess(output_data, confidence_threshold=0.5, nms_threshold=0.45, input_size=640):
    """
    Postprocesses the output of a TFLite model to get bounding boxes, scores, and class IDs.

    Args:
        output_data (np.ndarray): The output from the TFLite model.
        confidence_threshold (float): The confidence threshold for filtering detections.
        nms_threshold (float): The NMS threshold for removing overlapping boxes.

    Returns:
        tuple: A tuple containing lists of bounding boxes, scores, and class IDs.
    """
    boxes = []
    scores = []
    class_ids = []

    if output_data is None:
        return boxes, scores, class_ids

    out = np.asarray(output_data)
    if out.ndim == 3 and out.shape[0] == 1:
        out = out[0]
    if out.ndim != 2:
        return boxes, scores, class_ids

    # Ultralytics with nms=True usually yields [N, 6] with:
    # [x1, y1, x2, y2, confidence, class_id]
    if out.shape[1] == 6 or out.shape[0] == 6:
        detections = out if out.shape[1] == 6 else out.T
        # Detect normalised (0-1) coordinates and scale to pixel space
        if len(detections) > 0 and np.all(detections[:, :4] <= 1.5):
            detections = detections.copy()
            detections[:, [0, 2]] *= input_size
            detections[:, [1, 3]] *= input_size
        for det in detections:
            x1, y1, x2, y2, conf, cls_id = det
            if conf < confidence_threshold:
                continue
            w = max(0.0, float(x2 - x1))
            h = max(0.0, float(y2 - y1))
            if w <= 1.0 or h <= 1.0:
                continue
            boxes.append([int(round(x1)), int(round(y1)), int(round(w)), int(round(h))])
            scores.append(float(conf))
            class_ids.append(int(round(cls_id)))
        return boxes, scores, class_ids

    # Backward-compat path for raw YOLO output, e.g. [8400,84/85] or transposed.
    if out.shape[1] in (84, 85):
        raw = out
    elif out.shape[0] in (84, 85):
        raw = out.T
    else:
        return boxes, scores, class_ids

    has_obj = raw.shape[1] == 85
    class_start = 5 if has_obj else 4

    for det in raw:
        class_scores = det[class_start:]
        if class_scores.size == 0:
            continue
        cls_id = int(np.argmax(class_scores))
        cls_conf = float(class_scores[cls_id])
        obj_conf = float(det[4]) if has_obj else 1.0
        conf = obj_conf * cls_conf
        if conf < confidence_threshold:
            continue

        cx, cy, bw, bh = det[:4]
        x1 = float(cx - bw / 2.0)
        y1 = float(cy - bh / 2.0)
        boxes.append([x1, y1, float(bw), float(bh)])
        scores.append(conf)
        class_ids.append(cls_id)

    if not boxes:
        return [], [], []

    indices = cv2.dnn.NMSBoxes(boxes, scores, confidence_threshold, nms_threshold)
    if len(indices) == 0:
        return [], [], []

    final_boxes = []
    final_scores = []
    final_class_ids = []
    for i in indices.flatten():
        x, y, w, h = boxes[i]
        final_boxes.append([int(round(x)), int(round(y)), int(round(w)), int(round(h))])
        final_scores.append(float(scores[i]))
        final_class_ids.append(int(class_ids[i]))

    return final_boxes, final_scores, final_class_ids
