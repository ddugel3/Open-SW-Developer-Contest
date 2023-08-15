"""
# 검은색, 파란색 사각형 검출
import cv2
import numpy as np

image = cv2.imread("object recognition/Recognize EV up down/Data/엘베올라가는거.png")

lower_blue = np.array([105, 50, 0])
upper_blue = np.array([115, 250, 250])

lower_black = np.array([0,0,0])
upper_black = np.array([100,80,80])

hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
blue_mask = cv2.inRange(hsv_image, lower_blue, upper_blue)
blue_blurred_mask = cv2.blur(blue_mask, (10, 10))
contours, _ = cv2.findContours(blue_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

for contour in contours:
    x, y, w, h = cv2.boundingRect(contour)
    cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)


black_mask = cv2.inRange(hsv_image, lower_black, upper_black)
black_blurred_mask = cv2.blur(black_mask, (40, 40))
contours, _ = cv2.findContours(black_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

for contour in contours:
    x, y, w, h = cv2.boundingRect(contour)
    cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)
    
    
cv2.imshow("blurred", black_blurred_mask)
cv2.imshow("Result Image", image)
cv2.imshow("mask Image", black_mask)
cv2.waitKey(0)
cv2.destroyAllWindows()
"""

import cv2
import numpy as np

#image = cv2.imread("object recognition/Recognize EV up down/Data/엘베올라가는거.png")
image = cv2.imread("object recognition/Recognize EV up down/Data/down.jpg")

lower_blue = np.array([105, 50, 0])
upper_blue = np.array([115, 250, 250])

lower_black = np.array([0, 0, 0])
upper_black = np.array([100, 80, 80])

lower_orange = np.array([0, 120, 200])
upper_orange = np.array([20, 255, 255])

orange_hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
orange_mask = cv2.inRange(orange_hsv_image, lower_orange, upper_orange)
orange_blurred_mask = cv2.blur(orange_mask, (10, 10))
contours, _ = cv2.findContours(orange_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
flag_V = False
x_orange = None
y_orange = None
w_orange = None
h_orange = None


for contour in contours:
    x_orange, y_orange, w_orange, h_orange = cv2.boundingRect(contour)
    center_x = x_orange + w_orange // 2
    center_y = y_orange + h_orange // 2
    if abs(center_x - center_y) < 10:
        print("V자 형태가 아닙니다.")
    elif center_x < center_y:
        print("V자 형태: 아래로 향하는 V자")
        flag_V = True
    else:
        print("V자 형태: 위로 향하는 V자")
        flag_V = True
    cv2.rectangle(image, (x_orange, y_orange), (x_orange + w_orange, y_orange + h_orange), (0, 255, 0), 2)


hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
blue_mask = cv2.inRange(hsv_image, lower_blue, upper_blue)
blue_blurred_mask = cv2.blur(blue_mask, (20, 20))
blue_contours, _ = cv2.findContours(blue_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

black_mask = cv2.inRange(hsv_image, lower_black, upper_black)
black_blurred_mask = cv2.blur(black_mask, (30, 30))
black_contours, _ = cv2.findContours(black_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
flag_blue_in_black = False

for black_contour in black_contours:
    x_black, y_black, w_black, h_black = cv2.boundingRect(black_contour)
    for blue_contour in blue_contours:
        x_blue, y_blue, w_blue, h_blue = cv2.boundingRect(blue_contour)
        if flag_V:
            if x_blue > x_black and y_blue > y_black and x_blue + w_blue < x_black + w_black and y_blue + h_blue < y_black + h_black and x_orange > x_black and y_orange > y_black and x_orange + w_orange < x_black + w_black and y_orange + h_orange < y_black + h_black:
                flag_blue_in_black = True
                cv2.rectangle(image, (x_black, y_black), (x_black + w_black, y_black + h_black), (0, 255, 0), 2)
                cv2.rectangle(image, (x_blue, y_blue), (x_blue + w_blue, y_blue + h_blue), (0, 0, 255), 2)

print(flag_blue_in_black)

cv2.imshow("Result Image", image)
cv2.imshow("blue Image", blue_blurred_mask)


cv2.waitKey(0)
cv2.destroyAllWindows()
