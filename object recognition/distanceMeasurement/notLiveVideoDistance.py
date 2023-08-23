import cv2
import numpy as np
import time

vedio_path = 'Open-SW-Developer-Contest/object recognition/mortorcyle2.mp4'
min_confidence = 0.5

def calculate_distance(w, h, known_width, focal_length):
    # Calculate the distance from the camera to the object
    return (known_width * focal_length) / w

def detectAndDisplay(frame, focal_length):
    start_time = time.time()
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
            if confidence > min_confidence:
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
            label = "{}: {:.2f}".format(classes[class_ids[i]], confidences[i]*100)
            distance = calculate_distance(w, h, known_widths[classes[class_ids[i]]], focal_length)
            text = "{} - Distance: {:.2f} meters".format(label, distance)
            print(text)
            color = colors[i]
            cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
            cv2.putText(img, text, (x, y - 5), font, 1, color, 1)
    end_time = time.time()
    process_time = end_time - start_time
    print("=== A frame took {:.3f} seconds".format(process_time))
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
    'car': 3.0,     # 예: 자동차의 실제 너비가 2.0 미터라고 가정
    'tie': 0.3,
    'truck':4,
    'motorbike':2,
    'traffic light':2
    # 다른 객체들의 실제 너비도 추가해야 합니다.
}

layer_names = net.getLayerNames()
output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
colors = np.random.uniform(0, 255, size=(len(classes), 3))

cap = cv2.VideoCapture(vedio_path)

# Set the focal length of the camera (Update this value with the actual focal length)
focal_length = 100

if not cap.isOpened:
    print('--(!)Error opening video capture')
    exit(0)
while True:
    ret, frame = cap.read()
    if frame is None:
        print('--(!) No captured frame -- Break!')
        break

    detectAndDisplay(frame, focal_length)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cv2.destroyAllWindows()
