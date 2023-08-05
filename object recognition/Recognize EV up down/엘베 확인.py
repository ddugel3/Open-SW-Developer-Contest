"""
import cv2
import numpy as np

def find_v_direction(contour, image_height):
    # 컨투어의 높이 구하기
    (x, y, w, h) = cv2.boundingRect(contour)
    center_y = y + h // 2
    return "위" if center_y < image_height // 2 else "아래"

# 이미지 불러오기
image = cv2.imread("object recognition/Recognize EV up down/Data/down.jpg")  # 'your_image.jpg'에 분석하고자 하는 사진 파일 경로 입력

# 주황색 범위로 마스크 생성
#lower_orange = (60, 100, 150)
#upper_orange = (120, 155, 255)
lower_orange = (0,0,180)
upper_orange = (40,150,255)

orange_mask = cv2.inRange(image, lower_orange, upper_orange)

# Canny 에지 검출
edges = cv2.Canny(orange_mask, 100, 200)

# 컨투어 탐지
contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

# 가장 긴 컨투어 찾기
longest_contour = max(contours, key=cv2.contourArea)

# 꼭짓점 개수 확인
vertices = cv2.approxPolyDP(longest_contour, 0.02 * cv2.arcLength(longest_contour, True), True)
num_vertices = len(vertices)

# V자의 꼭짓점 판별
if num_vertices == 3:
    v_direction = find_v_direction(longest_contour, image.shape[0])
    print("V자의 꼭짓점은 {} 방향입니다.".format(v_direction))
else:
    print("V자의 꼭짓점이 아닙니다.")

# 이미지에 컨투어 그리기
cv2.drawContours(image, [longest_contour], -1, (0, 255, 0), 2)

# 결과 이미지 출력
cv2.imshow("orange",orange_mask)
cv2.imshow("Image with Contour", image)
cv2.waitKey(0)
cv2.destroyAllWindows()
"""

import cv2
import numpy as np

# 이미지 불러오기
image = cv2.imread("object recognition/Recognize EV up down/Data/down.jpg")  # 'your_image.jpg'에 분석하고자 하는 사진 파일 경로 입력

# 주황색 범위로 마스크 생성
lower_orange = (0, 0, 180)
upper_orange = (40, 150, 255)

orange_mask = cv2.inRange(image, lower_orange, upper_orange)

# 모폴로지 연산을 통해 도트 보완
kernel = np.ones((3, 3), np.uint8)
dilated_mask = cv2.dilate(orange_mask, kernel, iterations=2)

# 모폴로지 연산을 통해 부드러운 테두리 생성
smooth_mask = cv2.erode(dilated_mask, kernel, iterations=2)

# Canny 에지 검출
edges = cv2.Canny(smooth_mask, 100, 200)

# 컨투어 탐지
contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

# 가장 긴 컨투어 찾기
longest_contour = max(contours, key=cv2.contourArea)

# 꼭짓점 찾기
vertices = cv2.approxPolyDP(longest_contour, 0.02 * cv2.arcLength(longest_contour, True), True)

# 모든 꼭짓점 좌표 출력 및 빨간색 점으로 표시
print("꼭짓점 좌표:")
for vertex in vertices:
    x, y = vertex[0]
    print(vertex[0])
    cv2.circle(image, (x, y), 5, (0, 0, 255), -1)  # 빨간색 점으로 표시

# V자의 꼭짓점 판별
if len(vertices) == 3:
    # 꼭짓점을 y 좌표 기준으로 정렬
    sorted_vertices = sorted(vertices, key=lambda x: x[0][1])

    # 가장 y 좌표가 작은 2개의 꼭짓점이 위에 위치하고, 가장 y 좌표가 큰 1개의 꼭짓점이 아래에 위치하면 V자
    if sorted_vertices[0][0][1] < sorted_vertices[1][0][1] < sorted_vertices[2][0][1]:
        v_direction = "위"
    else:
        v_direction = "아래"
    print("V자의 꼭짓점은 {} 방향입니다.".format(v_direction))

    # V자 선 그리기
    vx, vy, _, _ = cv2.fitLine(sorted_vertices, cv2.DIST_L2, 0, 0.01, 0.01)
    slope = vy / vx

    height, width, _ = image.shape
    x1 = int(width / 2)
    y1 = int(height / 2)
    x2 = int(x1 + 100)
    y2 = int(y1 + slope * 100)

    cv2.line(image, (x1, y1), (x2, y2), (0, 255, 0), 2)

# 결과 이미지 출력
cv2.imshow("orange", orange_mask)
cv2.imshow("smooth", smooth_mask)
cv2.imshow("dilated", dilated_mask)
cv2.imshow("Image with V-shape", image)
cv2.waitKey(0)
cv2.destroyAllWindows()
