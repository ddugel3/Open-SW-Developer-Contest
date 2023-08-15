import cv2
import numpy as np

image = cv2.imread("object recognition/Recognize EV up down/Data/down.jpg")

lower_orange = np.array([10, 130, 200])
upper_orange = np.array([30, 255, 255])

hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
orange_mask = cv2.inRange(hsv_image, lower_orange, upper_orange)
blurred_mask = cv2.blur(orange_mask, (10, 10))
contours, _ = cv2.findContours(blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

for contour in contours:
    x, y, w, h = cv2.boundingRect(contour)
    center_x = x + w // 2
    center_y = y + h // 2
    if abs(center_x - center_y) < 10:
        print("V자 형태가 아닙니다.")
    elif center_x < center_y:
        print("V자 형태: 아래로 향하는 V자")
    else:
        print("V자 형태: 위로 향하는 V자")
    cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)

cv2.imshow("mask Image", orange_mask)
cv2.imshow("blurred", blurred_mask)
cv2.imshow("Result Image", image)
cv2.waitKey(0)
cv2.destroyAllWindows()