import cv2
import numpy as np
import requests
import time


from firebase_admin import credentials, initialize_app, storage, db


# Firebase Admin SDK 초기화
cred = credentials.Certificate('')
initialize_app(cred, {
    'serviceAccountId': 'firebase-adminsdk-b8iok@last-chance-276ca.iam.gserviceaccount.com',
    'storageBucket': 'gs://last-chance-276ca.appspot.com',
    'databaseURL': 'https://last-chance-276ca-default-rtdb.firebaseio.com/'
})

def perform_object_recognition():

    # Yolo 로드
    net = cv2.dnn.readNet('..\dataHee\yolov4-tiny.cfg', "..\dataHee\yolov4-tiny.weights")
    classes = []
    with open("..\dataHee\coco.names", "r") as f:
        classes = [line.strip() for line in f.readlines()]
    layer_names = net.getLayerNames()
    output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
    colors = np.random.uniform(0, 255, size=(len(classes), 3))


    # 다운로드할 이미지의 URL

    image_url = "https://firebasestorage.googleapis.com/v0/b/opencv-android-8e53d.appspot.com/o/item%2Fimage.jpg?alt=media&token=986c95f1-4651-419f-abf0-8d1a534bf92b"  # 이미지 URL을 여기에 입력하세요

    # 이미지 다운로드
    response = requests.get(image_url)

    # 다운로드가 성공적으로 완료되었는지 확인
    if response.status_code == 200:
        # 이미지를 바이너리 데이터로 저장
        with open("sample.png", "wb") as f:
            f.write(response.content)
        print("이미지 다운로드 완료")

        img = cv2.imread("sample.png")
        img = cv2.resize(img, None, fx=0.4, fy=0.4)
        height, width, channels = img.shape

        # Detecting objects
        blob = cv2.dnn.blobFromImage(img, 0.00392, (416, 416), (0, 0, 0), True, crop=False)
        net.setInput(blob)
        outs = net.forward(output_layers)

        # 정보를 화면에 표시
        class_ids = []
        confidences = []
        boxes = []
        for out in outs:
            for detection in out:
                scores = detection[5:]
                class_id = np.argmax(scores)
                confidence = scores[class_id]
                if confidence > 0.5:
                    # Object detected
                    center_x = int(detection[0] * width)
                    center_y = int(detection[1] * height)
                    w = int(detection[2] * width)
                    h = int(detection[3] * height)
                    # 좌표
                    x = int(center_x - w / 2)
                    y = int(center_y - h / 2)
                    boxes.append([x, y, w, h])
                    confidences.append(float(confidence))
                    class_ids.append(class_id)

        indexes = cv2.dnn.NMSBoxes(boxes, confidences, 0.5, 0.4)

        font = cv2.FONT_HERSHEY_PLAIN
        for i in range(len(boxes)):
            if i in indexes:
                x, y, w, h = boxes[i]
                recognition_result = str(classes[class_ids[i]])
                color = colors[i]
                cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
                cv2.putText(img, recognition_result, (x, y + 30), font, 3, color, 3)
        cv2.imshow("Image", img)
        cv2.waitKey(1000)
        cv2.destroyAllWindows()

        return recognition_result

    else:
        print("이미지 다운로드 실패. HTTP 상태 코드:", response.status_code)



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