import cv2
import numpy as np

def preprocess_for_tflite(image_path, input_size=(640, 640)):
    """
    Loads an image, resizes it, and preprocesses it for TFLite inference.

    Args:
        image_path (str): The path to the input image.
        input_size (tuple): The target size for the model input (width, height).

    Returns:
        np.ndarray: The preprocessed image tensor.
    """
    # Load the image using OpenCV
    image = cv2.imread(image_path)
    # Convert from BGR (OpenCV default) to RGB
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    # Resize the image to the model's expected input size
    image_resized = cv2.resize(image, input_size)

    # Normalize pixel values to the [0, 1] range
    # The model expects float32 input
    image_normalized = image_resized.astype(np.float32) / 255.0

    # Add a batch dimension to match the model's input shape [1, H, W, C]
    input_tensor = np.expand_dims(image_normalized, axis=0)

    return input_tensor

if __name__ == '__main__':
    # This is an example of how to use the function
    sample_image = 'sample.jpg'
    
    if not os.path.exists(sample_image):
        print(f"Sample image not found at '{sample_image}'. Please provide a sample image.")
    else:
        print(f"Preprocessing sample image: {sample_image}")
        input_tensor = preprocess_for_tflite(sample_image)
        
        print(f"Input tensor shape: {input_tensor.shape}")
        print(f"Input tensor dtype: {input_tensor.dtype}")
        print(f"Min pixel value: {np.min(input_tensor)}")
        print(f"Max pixel value: {np.max(input_tensor)}")

