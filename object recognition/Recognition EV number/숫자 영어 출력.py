import cv2
import os
try:
    from PIL import Image
except ImportError:
    import Image
import pytesseract

# 이미지 불러오기, RGB 프로세싱
image = cv2.imread("object recognition/Recognition_num/Data/1.png")

lower_orange = (60, 100, 150)
upper_orange = (120, 155, 255) 
orange_mask = cv2.inRange(image, lower_orange, upper_orange)


gray = cv2.cvtColor(orange_mask, cv2.COLOR_GRAY2BGR)

filename = "{}.png".format(os.getpid())
cv2.imwrite(filename, gray)


text = pytesseract.image_to_string(Image.open(filename), lang=None)
os.remove(filename)

print(text)

cv2.imshow("Image", image)
cv2.imshow("Gray Image", gray)
cv2.waitKey(0)
