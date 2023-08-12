import cv2

def mouse_callback(event, x, y, flags, param):
    if event == cv2.EVENT_LBUTTONDOWN:
        bgr_color = image[y, x]
        print("BGR Color: ", bgr_color)

# 이미지 불러오기, RGB 프로세싱
image = cv2.imread("object recognition/Recognize EV up down/Data/down.jpg")
cv2.imshow("Image", image)

# 마우스 콜백 함수 설정
cv2.setMouseCallback("Image", mouse_callback)

cv2.waitKey(0)
cv2.destroyAllWindows()