import cv2
import numpy as np


# 성별 확인
gender = input()

# 이미지 로드
image = cv2.imread("toiletimg.jpg")

# BGR에서 HSV로 변환
hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

# 추출할 색상 범위 설정 (RGB 색상 범위)
if gender == "female":
    lower_color = np.array([150, 30, 100])  # 하한값
    upper_color = np.array([255, 150, 200])  # 상한값

else :
    lower_color = np.array([40, 40, 40])  # 하한값
    upper_color = np.array([80, 255, 255])  # 상한값

# 색상 범위에 해당하는 부분 확인
mask = cv2.inRange(hsv_image, lower_color, upper_color)

# 바운딩 박스 그리기
contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)


for contour in contours:
    x, y, w, h = cv2.boundingRect(contour)
    cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)

    # 색상이 가운데 좌표보다 왼쪽에 있는지 오른쪽에 있는지 판단하여 메시지 출력
    center_x = image.shape[1] // 2
    if x + w // 2 < center_x:
        print("색상이 왼쪽에 있습니다. 왼쪽으로 가세요.")
    else:
        print("색상이 오른쪽에 있습니다. 오른쪽으로 가세요.")

# 결과 출력
cv2.imshow("Original Image", image)
cv2.waitKey(0)
cv2.destroyAllWindows()