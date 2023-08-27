import cv2
from numpy import *

test_imgs = ['closed.jpeg']

for imgFile in test_imgs:
    img = cv2.imread(imgFile)
    height, width, channels = img.shape
    mask = zeros((height+2, width+2), uint8)

    start_pixel = (510, 110)
    diff = (2, 2, 2)

    retval, img, mask, rect = cv2.floodFill(img, mask, start_pixel, (0, 255, 0), diff, diff)


    #check the size of the floodfilled area, if its large the door is closed:
    if retval > 10000:
        print(imgFile + ": 차고 문이 열렸습니다.")
    else:
        print(imgFile + ": 차고 문이 닫혔습니다.")

    cv2.imwrite(imgFile.replace(".jpg", "") + "_result.jpg", img)
