import cv2
import numpy as np
import requests
from io import BytesIO
import json
import sys
import os

# Get the directory where the script is located
script_dir = os.path.dirname(os.path.abspath(__file__))

# Paths to the YOLO files using absolute paths
YOLO_WEIGHTS = os.path.join(script_dir, 'yolov3.weights')
YOLO_CFG = os.path.join(script_dir, 'yolov3.cfg')
COCO_NAMES = os.path.join(script_dir, 'coco.names')

# Check if YOLO configuration files exist
if not os.path.isfile(YOLO_CFG):
    print(json.dumps({"status": "error", "message": f"YOLO configuration file not found at {YOLO_CFG}"}), file=sys.stderr)
    sys.exit(1)

if not os.path.isfile(YOLO_WEIGHTS):
    print(json.dumps({"status": "error", "message": f"YOLO weights file not found at {YOLO_WEIGHTS}"}), file=sys.stderr)
    sys.exit(1)

if not os.path.isfile(COCO_NAMES):
    print(json.dumps({"status": "error", "message": f"COCO names file not found at {COCO_NAMES}"}), file=sys.stderr)
    sys.exit(1)

# Load YOLO
net = cv2.dnn.readNet(YOLO_WEIGHTS, YOLO_CFG)

# To use GPU (if available), uncomment the following lines:
# net.setPreferableBackend(cv2.dnn.DNN_BACKEND_CUDA)
# net.setPreferableTarget(cv2.dnn.DNN_TARGET_CUDA)

# Load COCO class labels
with open(COCO_NAMES, 'r') as f:
    classes = [line.strip() for line in f.readlines()]

# Specify the classes you want to detect
TARGET_CLASSES = {'car', 'truck'}

# Get the output layer names
layer_names = net.getLayerNames()
try:
    output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers().flatten()]
except AttributeError:
    # For older OpenCV versions
    output_layers = [layer_names[i[0] - 1] for i in net.getUnconnectedOutLayers()]

def download_image_from_url(url):
    """
    Downloads an image from the specified URL and converts it to a NumPy array.

    Args:
        url (str): The URL of the image to download.

    Returns:
        np.ndarray: The image in OpenCV format, or None if the download fails.
    """
    try:
        response = requests.get(url)
        response.raise_for_status()  # Raise an error for bad status codes
        image_data = BytesIO(response.content)
        image = cv2.imdecode(np.frombuffer(image_data.read(), np.uint8), cv2.IMREAD_COLOR)
        if image is None:
            print(json.dumps({"status": "error", "message": f"Unable to decode the image from URL: {url}"}), file=sys.stderr)
        return image
    except requests.exceptions.RequestException as e:
        print(json.dumps({"status": "error", "message": f"Error downloading image from {url}: {e}"}), file=sys.stderr)
        return None

def detect_vehicles(image, confidence_threshold=0.5, nms_threshold=0.4):
    """
    Detects cars and trucks in an image.

    Args:
        image (np.ndarray): The image in OpenCV format.
        confidence_threshold (float): Minimum confidence to filter weak detections.
        nms_threshold (float): Non-Max Suppression threshold.

    Returns:
        int: The number of detected cars and trucks.
    """
    height, width, channels = image.shape

    # Create a blob from the image and perform a forward pass
    blob = cv2.dnn.blobFromImage(image, 
                                 scalefactor=1/255.0, 
                                 size=(416, 416), 
                                 swapRB=True, 
                                 crop=False)
    net.setInput(blob)
    outputs = net.forward(output_layers)

    # Initialize lists for detection data
    class_ids = []
    confidences = []
    boxes = []

    # Iterate over each output layer
    for output in outputs:
        for detection in output:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            # Filter detections by confidence and target classes (car and truck)
            if confidence > confidence_threshold and classes[class_id] in TARGET_CLASSES:
                # Object detected
                center_x = int(detection[0] * width)
                center_y = int(detection[1] * height)
                w = int(detection[2] * width)
                h = int(detection[3] * height)

                # Rectangle coordinates
                x = int(center_x - w / 2)
                y = int(center_y - h / 2)

                boxes.append([x, y, w, h])
                confidences.append(float(confidence))
                class_ids.append(class_id)

    # Apply Non-Max Suppression to eliminate redundant overlapping boxes with lower confidences
    indexes = cv2.dnn.NMSBoxes(boxes, confidences, confidence_threshold, nms_threshold)

    detected_vehicles = 0

    # Count detected vehicles
    if len(indexes) > 0:
        detected_vehicles = len(indexes.flatten())

    return detected_vehicles

def detect_vehicles_in_image_data(image_data_list, confidence_threshold=0.5, nms_threshold=0.4):
    """
    Detects cars and trucks in multiple images from URLs with predefined coordinates.

    Args:
        image_data_list (list): List of dictionaries containing 'url', 'threshold1', 'threshold2', and 'coordinates'.
        confidence_threshold (float, optional): Minimum confidence to filter weak detections. Defaults to 0.5.
        nms_threshold (float, optional): Non-Max Suppression threshold. Defaults to 0.4.

    Returns:
        list: A list of detection details corresponding to each image.
    """
    detection_details = []

    for idx, image_data in enumerate(image_data_list):
        url = image_data.get('url')
        threshold1 = image_data.get('threshold1')
        threshold2 = image_data.get('threshold2')
        coordinates = image_data.get('coordinates')  # List of [latitude, longitude] pairs

        print(f"Processing Image {idx+1}/{len(image_data_list)}: {url}", file=sys.stderr)
        image = download_image_from_url(url)
        if image is None:
            print(f"Skipping Image {idx+1} due to download error.", file=sys.stderr)
            detection_details.append({
                "imageUrl": url,
                "status": "failed",
                "coordinates": coordinates
            })
            continue

        detected_count = detect_vehicles(image, confidence_threshold, nms_threshold)
        print(f"Detected Vehicles: {detected_count}", file=sys.stderr)

        # Determine the result based on thresholds
        if detected_count < threshold1:
            result = 1
        elif threshold1 <= detected_count < threshold2:
            result = 2
        else:
            result = 3

        detection_details.append({
            "imageUrl": url,
            "detectedVehicles": detected_count,
            "classificationResult": result,
            "coordinates": coordinates  # Array of [latitude, longitude] pairs
        })
        print(f"Result for Image {idx+1}: {result}\n", file=sys.stderr)

    return detection_details

# Example usage
if __name__ == "__main__":
    # List of image data with URLs, thresholds, and multiple coordinates
    image_data_list = [
        {
            "url": "https://kamere.dars.si/kamere/Debeli_hrib/K21_V_debeli_hrib_iz_LJ.jpg",
            "threshold1": 5,
            "threshold2": 10,
            "coordinates": [
                [46.007771, 14.567476],
                [46.006756, 14.568364],
            ],
        },
        {
            "url": "https://kamere.dars.si/kamere/Tepanje/CP_Tepanje_jug.jpg",
            "threshold1": 5,
            "threshold2": 10,
            "coordinates": [
                [46.337746, 15.475098],
                [46.340279, 15.477752],
                [46.341397, 15.479007],
            ],
        },
        {
            "url": "https://www.drsc.si/kamere/KamSlike/Podplat/slike/Pdp1_0001.jpg",
            "threshold1": 0,
            "threshold2": 10,
            "coordinates": [
                [46.244052, 15.571120],
                [46.244524, 15.573787],
                [46.244677, 15.574974],
            ],
        },
        # Add more image data as needed
    ]

    # Detect vehicles and get results
    detection_results = detect_vehicles_in_image_data(image_data_list)

    # Output results as JSON to stdout
    print(json.dumps({"status": "success", "results": detection_results}))