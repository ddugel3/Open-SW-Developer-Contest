# Open-SW-Developer-Contest
2023 Open SW Developer Contest
Team : Roko

## **Introduction**
시각 장애인의 삶의 질을 향상하기 위한 목표로 ‘시각 장애인을 위한 위치 인식 기반 경로 및 보행 보이스 안내 어플’ 애플리케이션을 개발했습니다. 
주요 기능은 2가지로, Tmap 지도 API를 이용한 Navigation기능과 YOLO v4를 이용한 주변 사물 인식 및 안내입니다.
시각정보를 청각정보로 전달하기위해, TTS & STT 기능을 넣어서 음성 인식 및 텍스트 듣기를 제공합니다.


# 주요 기능

## 1. 기본구조
처음 앱을 실행하면, 사용자의 위치, 카메라, 음성 정보에 대한 권한 허용을 받고, 앱을 사용할 수 있다.

상단에는 출발지와 도착지를 입력받을 수 있는 edit text칸 두 개와 검색버튼, 출발지와 도착지를 서로 바꿀 수 있는 버튼이 위치해있고, 하단에는 음성안내 버튼과 자세한 정보 버튼이 위치해있다.

지도 화면이 띄워지고, 사용자의 현재 위치를 지도 위에 마커로 표시한다.

## 2. 출발지 자동 입력
출발지는 사용자의 현재위치로 정해져있고, 사용자가 원한다면 출발지를 새로 입력받아서 값 변경도 가능하다. 

## 3. 음성 인식으로 도착지 입력

## 4. 도착지까지 경로 디테일 알려주기

## 5. 보행 시 객체 인식 음성 안내

## 6. 객체 인식 




## 1. 중심지 찾기
입력된 Location들을 기반으로 중심지를 찾는 기능입니다.
1. 비교하고 싶은 Location들의 위치를 입력합니다.
2. 입력된 Location의 위도와 경도의 평균 값을 구합니다.
3. 해당 평균 값과 가장 인접한 지하철 역을 찾아 이용자에게 보여줍니다.


## 2. 시각장애인을 위한 Navigation
시각장애인을 위한 Navigation 기능입니다.
기본적으로 TTS & STT 기능이 들어가 있어 음성 인식 및 텍스트 듣기가 가능합니다.
1. 음성 인식 버튼을 클릭합니다.
2. 출발지 - 도착지 순서로 마이크를 통해 Location을 입력합니다.
3. 입력된 음성 데이터를 통해 출발지 - 도착지 Navigation 경로를 맵에 그립니다.
4. detail 버튼을 통해 해당 Navigation 경로를 음성으로 들을 수 있습니다.



## 🧑‍🏫 멘토 구성원
<table>
  <tr>
    <td align="center"><a href="https://github.com/ddugel3"><img src="https://avatars.githubusercontent.com/u/84488029?v=4" width="100px;" alt=""/><br /><sub><b>최건웅</b></sub></a><br /><a href="https://github.com/Kim-Jiyun" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/HeeNamgoong"><img src="https://avatars.githubusercontent.com/u/104904309?v=4" width="100px;" alt=""/><br /><sub><b>남궁희</b></sub></a><br /><a href="https://github.com/HeeNamgoong" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/bentshrimp"><img src="https://avatars.githubusercontent.com/u/39232867?v=4" width="100px;" alt=""/><br /><sub><b>박진우</b></sub></a><br /><a href="https://github.com/bentshrimp" title="Code">💻</a></td>  
    <td align="center"><a href="https://github.com/hyni03"><img src="https://avatars.githubusercontent.com/u/47711027?v=4" width="100px;" alt=""/><br /><sub><b>김은지</b></sub></a><br /><a href="https://github.com/hyni03" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/kmu-hyunwoo"><img src="https://avatars.githubusercontent.com/u/126188520?v=4" width="100px;" alt=""/><br /><sub><b>조현우</b></sub></a><br /><a href="https://github.com/kmu-hyunwoo" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/inqueue0979"><img src="https://avatars.githubusercontent.com/u/69336138?v=4" width="100px;" alt=""/><br /><sub><b>조원재</b></sub></a><br /><a href="https://github.com/inqueue0979" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/jooya38"><img src="https://avatars.githubusercontent.com/u/107492177?v=4" width="100px;" alt=""/><br /><sub><b>황연주</b></sub></a><br /><a href="https://github.com/jooya38" title="Code">💻</a></td>
  </tr>
  <tr>

</table>
