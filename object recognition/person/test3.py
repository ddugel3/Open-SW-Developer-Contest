import cv2

# 영상 경로
video_path = 'test/car.mp4'

# OpenCV 영상 객체 생성
cap = cv2.VideoCapture(video_path)

# 원하는 재생 속도 설정 (1.0은 원본 속도)
desired_speed = 2.0  # 2배속 (빠른 재생)

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    # 원하는 재생 속도에 따라 프레임 추가 또는 제거
    # desired_speed가 1보다 크면 프레임을 추가하고, 1보다 작으면 프레임을 제거합니다.
    num_frames_to_skip = int(1.0 / desired_speed)

    for _ in range(num_frames_to_skip):
        ret, frame = cap.read()
        if not ret:
            break

    # 영상 재생 속도 조절
    cv2.imshow('Video', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
