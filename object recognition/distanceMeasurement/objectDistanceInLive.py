import cv2
import numpy as np
import time

min_confidence = 0.5

def calculate_distance(w, h, known_width, focal_length):
    # Calculate the distance from the camera to the object
    return (known_width * focal_length) / w

def detectAndDisplay(frame, focal_length):
    img = cv2.resize(frame, None, fx=0.8, fy=0.8)
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
            if confidence > min_confidence and (classes[class_id] == 'person' or classes[class_id] == 'car' or classes[class_id] == 'truck' or classes[class_id] == 'bicycle' or classes[class_id] == 'motorbike' or classes[class_id] == 'dog') :  # Only consider 'person' class
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

            if class_name == 'person' or class_name == 'car' or classes[class_id] == 'truck' or class_name == 'bicycle' or classes[class_id] == 'motorbike' or classes[class_id] == 'dog':
                distance = calculate_distance(w, h, known_widths[class_name], focal_length)
                text = "{} - Distance: {:.2f} meters".format(label, distance)
                print(text)
            else:
                text = label

            color = colors[i]
            cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
            cv2.putText(img, text, (x, y - 5), font, 1, color, 1)

    cv2.imshow("YOLO test", img)

model_file = 'Open-SW-Developer-Contest/object recognition/person/yolov4-tiny.weights'
config_file = 'Open-SW-Developer-Contest/object recognition/person/yolov4-tiny.cfg'
net = cv2.dnn.readNet(model_file, config_file)

classes = []
with open("Open-SW-Developer-Contest/object recognition/person/coco.names", "r") as f:
    classes = [line.strip() for line in f.readlines()]

# 객체 별 실제 너비 (미터 단위)를 알고 있어야 합니다.
known_widths = {
    'person': 1.5,  # 예: 사람의 실제 너비가 1.5 미터라고 가정
    'cell phone': 0.3,     # 예: 자동차의 실제 너비가 2.0 미터라고 가정
    'chair': 1.0,
    'traffic light': 3,
    'car' : 3.0,
    'truck' : 4.0,
    'bicycle': 1.0,
    'motorbike': 2.0,
    'dog': 1.0
    # 다른 객체들의 실제 너비도 추가해야 합니다.
}

layer_names = net.getLayerNames()
output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
colors = np.random.uniform(0, 255, size=(len(classes), 3))

cap = cv2.VideoCapture(0)  # 노트북 내장 웹캠(카메라 인덱스 0)을 사용합니다.

# Set the focal length of the camera (Update this value with the actual focal length)
focal_length = 100

if not cap.isOpened():
    print('--(!)Error opening video capture')
    exit(0)

while True:
    ret, frame = cap.read()
    if not ret:
        print('--(!) No captured frame -- Break!')
        break

    detectAndDisplay(frame, focal_length)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()