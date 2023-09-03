import cv2
import numpy as np

min_confidence = 0.5

def calculate_distance(w, h, known_width, focal_length):
    # Calculate the distance from the camera to the object
    return (known_width * focal_length) / w

def detectAndDisplay(image, focal_length):
    img = cv2.imread(image)  # Load the static image
    height, width, channels = img.shape

    blob = cv2.dnn.blobFromImage(img, 0.00392, (416, 416), (0, 0, 0), True, crop=False)

    net.setInput(blob)
    outs = net.forward(output_layers)

    class_ids = []
    confidences = []
    boxes = []

    for out in outs:
        for detection in out:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            
            # Only consider predefined objects
            if confidence > min_confidence and (classes[class_id] == 'person' or classes[class_id] == 'car' or classes[class_id] == 'truck' 
                                                or classes[class_id] == 'bicycle' or classes[class_id] == 'motorbike' or classes[class_id] == 'dog' 
                                                or classes[class_id] == 'traffic light'):
                center_x = int(detection[0] * width)
                center_y = int(detection[1] * height)
                w = int(detection[2] * width)
                h = int(detection[3] * height)

                x = int(center_x - w / 2)
                y = int(center_y - h / 2)

                boxes.append([x, y, w, h])
                confidences.append(float(confidence))
                class_ids.append(class_id)

    indexes = cv2.dnn.NMSBoxes(boxes, confidences, min_confidence, 0.4)
    font = cv2.FONT_HERSHEY_DUPLEX
    for i in range(len(boxes)):
        if i in indexes:
            x, y, w, h = boxes[i]
            class_name = classes[class_ids[i]]
            label = "{}: {:.2f}".format(class_name, confidences[i]*100)

            # Object name & distance display
            if class_name == 'person' or class_name == 'car' or class_name == 'truck' or class_name == 'bicycle' or class_name == 'motorbike' or class_name == 'dog' or class_name == 'traffic light':
                distance = calculate_distance(w, h, known_widths[class_name], focal_length)
                text = "{} - Distance: {:.2f} meters".format(label, distance)
                
                # Traffic light color recognition (red, green)
                if class_name == 'traffic light':
                    roi = img[y:y + h, x:x + w]
                    hsv_roi = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
                    mask_red = cv2.inRange(hsv_roi, (0, 100, 100), (10, 255, 255))
                    mask_green = cv2.inRange(hsv_roi, (35, 100, 100), (85, 255, 255))
                    if np.sum(mask_red) > np.sum(mask_green):
                        text += " - Red"
                    else:
                        text += " - Green"
                        
                print(text)
            else:
                text = label

            color = colors[i]
            cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
            cv2.putText(img, text, (x, y - 5), font, 1, color, 1)

    cv2.imshow("YOLO test", img)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

# yolov configuration file paths
model_file = 'object recognition\dataHee\yolov4-tiny.weights'
config_file = 'object recognition\dataHee\yolov4-tiny.cfg'
net = cv2.dnn.readNet(model_file, config_file)

classes = []
with open("object recognition\dataHee\coco.names", "r") as f:
    classes = [line.strip() for line in f.readlines()]

# Object's actual widths (in meters) should be known.
known_widths = {
    'person': 1.5,  # Example: Assume the actual width of a person is 1.5 meters / Necessary objects
    'cell phone': 0.3,
    'chair': 1.0,
    'traffic light': 3,  # Necessary objects
    'car': 3.0,  # Necessary objects
    'truck': 4.0,  # Necessary objects
    'bicycle': 1.0,  # Categorized as obstacles
    'motorbike': 2.0,  # Categorized as obstacles
    'dog': 1.0,  # Categorized as obstacles
    # Add actual widths for other objects as well.
}

layer_names = net.getLayerNames()
output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
colors = np.random.uniform(0, 255, size=(len(classes), 3))

# Specify the path to your static image here
image_path = 'webserver\person.PNG' # storage에 저장될 프레임 단위의 이미지 경로 설정 

# Set the focal length of the camera (Update this value with the actual focal length)
focal_length = 100

detectAndDisplay(image_path, focal_length)
