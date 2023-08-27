import cv2
import numpy as np

# 자판기 이미지 로드
vending_machine_img = cv2.imread("vendingmachine.jpg", cv2.IMREAD_GRAYSCALE)

# 템플릿 이미지 로드 (자판기를 찾기 위한 작은 이미지)
template_img = cv2.imread("template.jpg", cv2.IMREAD_GRAYSCALE)

# 템플릿 매칭 수행
result = cv2.matchTemplate(vending_machine_img, template_img, cv2.TM_CCOEFF_NORMED)
min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(result)

# 템플릿과 일치하는 영역의 위치를 가져옴
top_left = max_loc
bottom_right = (top_left[0] + template_img.shape[1], top_left[1] + template_img.shape[0])

# 원본 이미지에 사각형 표시
cv2.rectangle(vending_machine_img, top_left, bottom_right, 255, 2)

# 자판기가 발견되었는지 여부 출력
if max_val > 0.9:
    print("자판기가 발견되었습니다.")
else:
    print("자판기를 찾을 수 없습니다.")

# 결과 이미지 출력
cv2.imshow("Detected Vending Machine", vending_machine_img)
cv2.waitKey(0)
cv2.destroyAllWindows()