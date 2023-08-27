import cv2
import numpy as np

image = cv2.imread("object recognition/Recognize EV up down/Data/down.jpg")

# 주황색 V자 검출 코드
lower_orange = np.array([0, 120, 200])
upper_orange = np.array([20, 255, 255])

hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
orange_mask = cv2.inRange(hsv_image, lower_orange, upper_orange)
orange_blurred_mask = cv2.blur(orange_mask, (10, 10))
orange_contours, _ = cv2.findContours(orange_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

# 파란색 박스 검출
lower_blue = np.array([105, 50, 0])
upper_blue = np.array([115, 250, 250])

blue_mask = cv2.inRange(hsv_image, lower_blue, upper_blue)
blue_blurred_mask = cv2.blur(blue_mask, (10, 10))
blue_contours, _ = cv2.findContours(blue_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

# 주황색 V자 하단 이격 거리와 V자 모양 판별
for orange_contour in orange_contours:
    orange_x, orange_y, orange_w, orange_h = cv2.boundingRect(orange_contour)
    
    # 주황색 V자 하단 좌표
    orange_bottom_x = orange_x + orange_w // 2
    orange_bottom_y = orange_y + orange_h
    
    for blue_contour in blue_contours:
        blue_x, blue_y, blue_w, blue_h = cv2.boundingRect(blue_contour)
        
        # 파란색 박스 중심 좌표
        blue_center_x = blue_x + blue_w // 2
        blue_center_y = blue_y + blue_h // 2
        
        # 주황색 V자 하단과 파란색 박스 중심의 거리 계산
        distance = np.sqrt((orange_bottom_x - blue_center_x)**2 + (orange_bottom_y - blue_center_y)**2)
        
        if distance < 500:  # 이격 거리가 50 이하인 경우
            # 주황색 V자 모양 판별
            center_x = orange_x + orange_w // 2
            center_y = orange_y + orange_h // 2
            if abs(center_x - center_y) < 10:
                print("V자 형태가 아닙니다.")
            elif center_x < center_y:
                print("V자 형태: 아래로 향하는 V자")
            else:
                print("V자 형태: 위로 향하는 V자")
            cv2.rectangle(image, (orange_x, orange_y), (orange_x + orange_w, orange_y + orange_h), (0, 255, 0), 2)
            cv2.rectangle(image, (blue_x, blue_y), (blue_x + blue_w, blue_y + blue_h), (0, 0, 255), 2)

cv2.imshow("Result Image", image)
cv2.imshow("blurred Orange Mask", orange_blurred_mask)
cv2.imshow("blurred Blue Mask", blue_blurred_mask)
cv2.imshow("mask Image", orange_mask)
cv2.imshow("blue mask Image", blue_mask)
cv2.waitKey(0)
cv2.destroyAllWindows()
