import cv2
import numpy as np

# 영상을 읽어옵니다. 카메라를 사용하려면 0 대신 카메라 번호를 사용합니다.
video_capture = cv2.VideoCapture(0)

# 주황색 V자 범위
lower_orange = np.array([0, 120, 200])
upper_orange = np.array([20, 255, 255])

# 파란색 박스 범위
lower_blue = np.array([105, 50, 0])
upper_blue = np.array([115, 250, 250])

while video_capture.isOpened():
    ret, frame = video_capture.read()
    if not ret:
        break
    
    hsv_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    
    # 파란색 박스 검출
    blue_mask = cv2.inRange(hsv_frame, lower_blue, upper_blue)
    blue_blurred_mask = cv2.blur(blue_mask, (20, 20))  # 강한 블러 처리
    blue_contours, _ = cv2.findContours(blue_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    if len(blue_contours) == 0:
        print("파란색 박스가 검출되지 않았습니다.")
        continue
    
    for blue_contour in blue_contours:
        blue_x, blue_y, blue_w, blue_h = cv2.boundingRect(blue_contour)
        
        # 주황색 V자 하단 좌표
        orange_bottom_x = blue_x + blue_w // 2
        orange_bottom_y = blue_y + blue_h
        
        # 주황색 V자 검출
        orange_mask = cv2.inRange(hsv_frame, lower_orange, upper_orange)
        orange_blurred_mask = cv2.blur(orange_mask, (10, 10))
        orange_contours, _ = cv2.findContours(orange_blurred_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        
        for orange_contour in orange_contours:
            orange_x, orange_y, orange_w, orange_h = cv2.boundingRect(orange_contour)
            
            # 주황색 V자 하단과 파란색 박스 중심의 거리 계산
            distance = np.sqrt((orange_bottom_x - (orange_x + orange_w // 2))**2 + (orange_bottom_y - (orange_y + orange_h // 2))**2)
            
            if distance < 500:  # 이격 거리가 500 이하인 경우
                # 주황색 V자 모양 판별
                center_x = orange_x + orange_w // 2
                center_y = orange_y + orange_h // 2
                if abs(center_x - center_y) < 10:
                    text = "V자 형태가 아닙니다."
                elif center_x < center_y:
                    text = "V자 형태: 아래로 향하는 V자"
                else:
                    text = "V자 형태: 위로 향하는 V자"
                
                cv2.putText(frame, text, (orange_x, orange_y), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
                cv2.rectangle(frame, (orange_x, orange_y), (orange_x + orange_w, orange_y + orange_h), (0, 255, 0), 2)
                cv2.rectangle(frame, (blue_x, blue_y), (blue_x + blue_w, blue_y + blue_h), (0, 0, 255), 2)
    
    cv2.imshow("Result Frame", frame)
    
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

video_capture.release()
cv2.destroyAllWindows()
