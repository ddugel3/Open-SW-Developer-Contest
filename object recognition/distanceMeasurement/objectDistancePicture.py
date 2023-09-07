import cv2
import numpy as np
import requests
import time

from firebase_admin import credentials, initialize_app, storage, db

# Firebase Admin SDK 초기화
cred = credentials.Certificate('opensw-ffe60-firebase-adminsdk-2773j-6df968948e.json')
initialize_app(cred, {
    'serviceAccountId': 'firebase-adminsdk-2773j@opensw-ffe60.iam.gserviceaccount.com',
    'storageBucket': 'gs://opensw-ffe60.appspot.com',
    'databaseURL': 'https://opensw-ffe60-default-rtdb.firebaseio.com/'
})


min_confidence = 0.5


def calculate_distance(w, h, known_width, focal_length):
    # Calculate the distance from the camera to the object
    return (known_width * focal_length) / w

  
def perform_object_recognition():
    # Set the focal length of the camera (Update this value with the actual focal length)
    focal_length = 100

    text = ''

    # 다운로드할 이미지의 URL

    image_url = "https://firebasestorage.googleapis.com/v0/b/opensw-ffe60.appspot.com/o/item%2Fimage.jpg?alt=media&token=d0355587-80bf-4062-8f31-c3c5cbf903a5"  # 이미지 URL을 여기에 입력하세요

    # 이미지 다운로드
    response = requests.get(image_url)

    # 다운로드가 성공적으로 완료되었는지 확인
    if response.status_code == 200:
        # 이미지를 바이너리 데이터로 저장
        with open("sample.png", "wb") as f:
            f.write(response.content)
        print("이미지 다운로드 완료")


        img = cv2.imread("sample.png")
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
                    #text = "{} Distance {:.2f} meters".format(label, distance)
                    if class_name == "person":
                        text = "전방 {:.2f}미터 앞에 사람이 있습니다.".format(distance)
                    elif class_name == "car" :
                        text = "전방 {:.2f}미터 앞에 차량이 있습니다.".format(distance)
                    elif (class_name == "truck"):
                        text = "전방 {:.2f}미터 앞에 트럭이 있습니다.".format(distance)
                    elif (class_name == "bicycle"):
                        text = "전방 {:.2f}미터 앞에 자전거가 있습니다.".format(distance)

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

        # cv2.imshow("YOLO test", img)
        # cv2.waitKey(1000)
        # cv2.destroyAllWindows()

        return text

# yolov configuration file paths
model_file = '..\dataHee\yolov4-tiny.weights'
config_file = '..\dataHee\yolov4-tiny.cfg'
net = cv2.dnn.readNet(model_file, config_file)

classes = []
with open("..\dataHee\coco.names", "r") as f:
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


while True:
    try:
        # Firebase Realtime Database 참조 가져오기
        ref = db.reference()

        # 2초마다 객체 인식 수행
        result = perform_object_recognition()

        # 결과를 Firebase Realtime Database에 저장
        new_result_ref = ref.child('object_recognition_results').set({"message": result})

        # 저장된 결과의 고유 키(데이터베이스 ID)를 얻을 수 있습니다.
        result_key = new_result_ref.key

        # 2초 대기
        time.sleep(5)

    except Exception as e:
        # 오류가 발생하면 해당 오류를 출력하고 계속 진행합니다.
        print("An error occurred:", str(e))


