import cv2
import numpy as np
from skimage.metrics import mean_squared_error

def determine_arrow_direction(contour, frame):
    x_orange, y_orange, w_orange, h_orange = cv2.boundingRect(contour)
    center_x = x_orange + w_orange // 2
    center_y = y_orange + h_orange // 2

    if abs(center_x - center_y) < 10:
        direction = "V자 형태가 아닙니다."
    elif center_x < center_y:
        direction = "아래로 향하는 V자"
    else:
        direction = "위로 향하는 V자"
    
    cv2.rectangle(frame, (x_orange, y_orange), (x_orange + w_orange, y_orange + h_orange), (0, 255, 0), 2)
    return direction

# 이미지 불러오기
image = cv2.imread('object recognition/Recognize EV up down/Data/1.png')
#image = cv2.imread('object recognition/Recognize EV up down/Data/down.jpg')

hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

lower_orange = np.array([0, 130, 200])
upper_orange = np.array([20, 255, 255])

orange_mask = cv2.inRange(hsv_image, lower_orange, upper_orange)
mask = cv2.blur(orange_mask, (10, 10))
contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

arrow_detected = False  # 화살표 검출 여부를 나타내는 변수

for contour in contours:
    area = cv2.contourArea(contour)
    min_area_threshold = 250
    if area > min_area_threshold:
        x, y, w, h = cv2.boundingRect(contour)
        cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)

def average_hash(image):
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    resized_image = cv2.resize(image, (8, 8))
    avg = np.mean(resized_image)
    binary_image = (resized_image > avg).astype(np.uint8)
    return binary_image.flatten()

arrow_template = cv2.imread('object recognition/Recognize EV up down/Data/위.png')
#arrow_template = cv2.imread('object recognition/Recognize EV up down/Data/아래.png')

template_hash = average_hash(arrow_template)

for contour in contours:
    x, y, w, h = cv2.boundingRect(contour)
    arrow_roi = image[y:y+h, x:x+w]
    arrow_hash = average_hash(arrow_roi)

    similarity = 1 - mean_squared_error(template_hash, arrow_hash)
    similarity_threshold = 0.43 
    if similarity > similarity_threshold:
        arrow_detected = True  # 화살표가 검출됨을 표시
        cv2.rectangle(image, (x, y), (x + w, y + h), (255, 0, 0), 2)
        arrow_direction = determine_arrow_direction(contour, image)
        cv2.putText(image, arrow_direction, (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 2)

# 화살표가 검출되었을 경우 콘솔 메시지 출력
if arrow_detected:
    print("검출되었습니다. 방향:", arrow_direction)
else:
    print("화살표가 없습니다.")        


cv2.imshow("Result", image)
cv2.waitKey(0)
cv2.destroyAllWindows()


