import cv2
import numpy as np

# 웹캠을 열어서 비디오 입력을 받습니다.
cap = cv2.VideoCapture(0)

# 주황색 범위를 정의합니다.
lower_orange = np.array([0, 120, 200])
upper_orange = np.array([20, 255, 255])

while True:
    # 비디오에서 프레임을 읽어옵니다.
    ret, frame = cap.read()
    
    if not ret:
        break

    # 이미지를 HSV 색 공간으로 변환합니다.
    hsv_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

    # 주황색 범위에 해당하는 마스크를 생성합니다.
    orange_mask = cv2.inRange(hsv_frame, lower_orange, upper_orange)

    # 마스크를 블러 처리하여 빈 공간을 채웁니다.
    blurred_mask = cv2.blur(orange_mask, (10, 10))

    # 주황색 영역을 찾습니다.
    contours, _ = cv2.findContours(blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # 주황색 V자 판독을 위한 로직을 구현합니다.
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
        cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)

    # 결과 프레임을 표시합니다.
    cv2.imshow("Result Frame", frame)
    cv2.imshow("mask Frame", orange_mask)
    
    # 'q' 키를 누르면 루프를 종료합니다.
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# 웹캠을 해제하고 창을 닫습니다.
cap.release()
cv2.destroyAllWindows()
