# Open-SW-Developer-Contest
2023 Open SW Developer Contest
Team : Roko

## **Introduction**
시각 장애인의 삶의 질을 향상하기 위한 목표로 ‘시각 장애인을 위한 위치 인식 기반 경로 및 보행 보이스 안내 어플’ 애플리케이션을 개발했습니다. 
주요 기능은 2가지로, Tmap 지도 API를 이용한 Navigation기능과 YOLO v4를 이용한 주변 사물 인식 및 안내입니다.
시각정보를 청각정보로 전달하기위해, TTS & STT 기능을 넣어서 음성 인식 및 텍스트 듣기를 제공합니다.

## 앱 UI
<img src="https://github.com/ddugel3/Open-SW-Developer-Contest/assets/104904309/d9491df3-c246-4d4f-8429-d2b16ad35c9b" width="200" height="400"/>
<img src="https://github.com/ddugel3/Open-SW-Developer-Contest/assets/104904309/a02ace6a-5de3-4b60-9220-4853f9ef0499" width="200" height="400"/>

<img src="https://github.com/ddugel3/Open-SW-Developer-Contest/assets/104904309/7b1d29f7-7569-4397-b01f-0aa9f8cf2735" width="200" height="400"/>

<img src="https://github.com/ddugel3/Open-SW-Developer-Contest/assets/104904309/4ac94c09-162d-40ab-8a32-8c8b40ece75c" width="200" height="400"/>




# 주요 기능

## 1. 기본구조
앱을 실행하면 사용자의 위치, 카메라, 음성 정보에 대한 권한 허용을 받고, 앱을 사용할 수 있다.

상단에는 출발지와 도착지를 입력받을 수 있는 edit text칸 두 개와 검색버튼, 출발지와 도착지를 서로 바꿀 수 있는 버튼이 위치해있고, 하단에는 음성안내 버튼과 자세한 정보 버튼이 제공된다.

지도 화면이 띄워지고, 사용자의 현재 위치를 지도 위에 마커로 표시한다.

## 2. 출발지 자동 입력
출발지는 사용자의 현재위치로 자동 설정되며, 사용자가 원한다면 출발지를 새로 입력받아서 값 변경도 가능하다. 

## 3. 음성 인식으로 도착지 입력
사용자가 도착지를 음성으로 입력할 수 있도록 음성 인식 버튼을 제공한다.
Google 음성 인식 기능을 사용하여 사용자가 말한 내용을 인식하고 도착지 입력란에 자동으로 채운다.

## 4. 도착지까지 경로 디테일 알려주기
검색 버튼을 클릭하면 출발지에서 도착지까지의 전체 경로를 지도에 표시한다.
Tmap API를 활용하여 경로를 표시하고 안내한다.

## 5. 보행 시 객체 인식 음성 안내
자세한 정보 버튼을 클릭하면 경로의 상세 정보가 표시되며, 음성으로 읽어주는 길 안내 음성 안내가 시작된다.
TTS 기능을 사용하여 경로 상세 정보를 음성으로 제공한다.

## 6. 객체 인식
시각 장애인이 보행 중에 위험한 사물이나 장애물을 감지하기 위해 객체 인식 기능이 구현되어있다.
YOLO v4를 사용하여 사람, 자동차, 강아지, 오토바이, 자전거, 신호등 등 다양한 객체를 인식하고 해당 객체와의 거리를 제공한다.
객체 인식 결과를 음성으로 안내하여 시각 장애인의 안전한 보행을 지원한다.

## 7. 앱과 객체 인식 연동
모바일 앱에서 실시간 카메라 프리뷰를 가져와 실시간으로 이미지를 캡처하고 Firebase Storage에 업로드한다.
파이썬을 사용하여 이미지에서 객체를 인식하고 결과를 Firebase Realtime Database에 저장한다.
앱은 Firebase를 통해 실시간 객체 인식 결과를 받아와 사용자에게 제공한다.

<br>

## :blush: 팀 구성원 
<table>
  <tr>
    <td align="center"><a href="https://github.com/ddugel3"><img src="https://avatars.githubusercontent.com/u/56158371?v=4" width="100px;" alt=""/><br /><sub><b>최건웅</b></sub></a><br /><a href="https://github.com/ddugel3" title="Code">💻</a></td>
        <td align="center"><a href="https://github.com/inqueue0979"><img src="https://avatars.githubusercontent.com/u/105335065?v=4" width="100px;" alt=""/><br /><sub><b>김민주</b></sub></a><br /><a href="https://github.com/mjk25" title="Code">💻</a></td>
        <td align="center"><a href="https://github.com/hyni03"><img src="https://avatars.githubusercontent.com/u/47711027?v=4" width="100px;" alt=""/><br /><sub><b>김은지</b></sub></a><br /><a href="https://github.com/hyni03" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/HeeNamgoong"><img src="https://avatars.githubusercontent.com/u/104904309?v=4" width="100px;" alt=""/><br /><sub><b>남궁희</b></sub></a><br /><a href="https://github.com/HeeNamgoong" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/docherryra"><img src="https://avatars.githubusercontent.com/u/104899885?v=4" width="100px;" alt=""/><br /><sub><b>한이연</b></sub></a><br /><a href="https://github.com/jooya38" title="Code">💻</a></td>
  </tr>
  <tr>


</table>
