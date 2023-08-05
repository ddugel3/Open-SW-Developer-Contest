import cv2
import os
try:
    from PIL import Image
except ImportError:
    import Image
import pytesseract


cap = cv2.VideoCapture('object recognition/Recognition_num/Data/number.mov')  

lower_orange = (60, 100, 150)
upper_orange = (120, 155, 255)

while True:
    ret, frame = cap.read()

    if not ret:
        break

    orange_mask = cv2.inRange(frame, lower_orange, upper_orange)

    gray = cv2.cvtColor(orange_mask, cv2.COLOR_GRAY2BGR)

    text = pytesseract.image_to_string(Image.fromarray(gray), lang=None)

    print(text)

    cv2.imshow('Video', frame)
    cv2.imshow('Orange Text', gray)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
