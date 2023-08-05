import cv2
import numpy as np

# Load YOLO model and classes
net = cv2.dnn.readNet("/home/ngh/publicSoftWareContest/Open-SW-Developer-Contest/object recognition/person/yolov3-tiny.cfg", "/home/ngh/publicSoftWareContest/Open-SW-Developer-Contest/object recognition/person/yolov3-tiny.weights")
classes = []
with open("/home/ngh/publicSoftWareContest/Open-SW-Developer-Contest/object recognition/person/coco.names", "r") as f:
    classes = [line.strip() for line in f.readlines()]

# Set the classes to detect (in this case, we only want to detect 'person')
person_class_id = classes.index("person")
net.setPreferableBackend(cv2.dnn.DNN_BACKEND_OPENCV)
net.setPreferableTarget(cv2.dnn.DNN_TARGET_CPU)

def detect_person(frame):
    height, width, _ = frame.shape
    blob = cv2.dnn.blobFromImage(frame, 0.00392, (416, 416), (0, 0, 0), True, crop=False)
    net.setInput(blob)
    outs = net.forward(get_output_layers(net))

    class_ids = []
    confidences = []
    boxes = []
    for out in outs:
        for detection in out:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            if class_id == person_class_id and confidence > 0.5:
                center_x = int(detection[0] * width)
                center_y = int(detection[1] * height)
                w = int(detection[2] * width)
                h = int(detection[3] * height)
                x = int(center_x - w / 2)
                y = int(center_y - h / 2)
                class_ids.append(class_id)
                confidences.append(float(confidence))
                boxes.append([x, y, w, h])

    return class_ids, confidences, boxes

def get_output_layers(net):
    layer_names = net.getLayerNames()
    output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
    return output_layers

def calculate_distance(w, h, focal_length, known_width, known_distance):
    # Calculate the distance from the camera to the object
    return (known_width * focal_length) / w

def main():
    cap = cv2.VideoCapture(0)
    focal_length = 100  # Update this value with the actual focal length of your camera
    known_width = 0.5  # Update this value with the actual width of the object being used for calibration
    known_distance = 2.0  # Update this value with the actual distance of the object being used for calibration

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        class_ids, confidences, boxes = detect_person(frame)

        for i in range(len(boxes)):
            x, y, w, h = boxes[i]
            distance = calculate_distance(w, h, focal_length, known_width, known_distance)
            text = f"Distance: {distance:.2f} meters"
            cv2.putText(frame, text, (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)

        cv2.imshow("Object Detection", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
