import cv2
import numpy as np
import time # -- 프레임 계산을 위해 사용


vedio_path = 'test/car.mp4' #-- 사용할 영상 경로
min_confidence = 0.5

def detectAndDisplay(frame):
    start_time = time.time()
    img = cv2.resize(frame, None, fx=0.8, fy=0.8)
    height, width, channels = img.shape
    #cv2.imshow("Original Image", img)

    #-- 창 크기 설정
    blob = cv2.dnn.blobFromImage(img, 0.00392, (416, 416), (0, 0, 0), True, crop=False)

    net.setInput(blob)
    outs = net.forward(output_layers)

    #-- 탐지한 객체의 클래스 예측 
    class_ids = []
    confidences = []
    boxes = []

    for out in outs:
        for detection in out:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            if confidence > min_confidence:
                # 탐지한 객체 박싱
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
            print(i, label)
            color = colors[i] #-- 경계 상자 컬러 설정 / 단일 생상 사용시 (255,255,255)사용(B,G,R)
            cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
            cv2.putText(img, label, (x, y - 5), font, 1, color, 1)
    end_time = time.time()
    process_time = end_time - start_time
    print("=== A frame took {:.3f} seconds".format(process_time))
    cv2.imshow("YOLO test", img)
    
#-- yolo 포맷 및 클래스명 불러오기
model_file = 'test/yolov3.weights' #-- 본인 개발 환경에 맞게 변경할 것
config_file = 'test/yolov3.cfg' #-- 본인 개발 환경에 맞게 변경할 것
net = cv2.dnn.readNet(model_file, config_file)


#-- GPU 사용
#net.setPreferableBackend(cv2.dnn.DNN_BACKEND_CUDA)
#net.setPreferableTarget(cv2.dnn.DNN_TARGET_CUDA)

#-- 클래스(names파일) 오픈 / 본인 개발 환경에 맞게 변경할 것
classes = []
with open("test/coco.names", "r") as f:
    classes = [line.strip() for line in f.readlines()]
layer_names = net.getLayerNames()
output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
colors = np.random.uniform(0, 255, size=(len(classes), 3))

#-- 비디오 활성화
cap = cv2.VideoCapture(vedio_path) #-- 웹캠 사용시 vedio_path를 0 으로 변경

# desired_speed = 2.0

if not cap.isOpened:
    print('--(!)Error opening video capture')
    exit(0)
while True:
    ret, frame = cap.read()
    if frame is None:
        print('--(!) No captured frame -- Break!')
        break


    # num_frames_to_skip = int(1.0 / desired_speed)

    # for _ in range(num_frames_to_skip):
    #     ret, frame = cap.read()
    #     if not ret:
    #         break

    detectAndDisplay(frame)
    #-- q 입력시 종료
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cv2.destroyAllWindows()

