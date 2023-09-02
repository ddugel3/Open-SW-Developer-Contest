package com.example.open_sw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;

import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.MapView;

import android.speech.RecognizerIntent;

import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.util.FusedLocationSource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button search_btn;
    private Button setting_btn;

    private MapView mapView;
    private static NaverMap naverMap;

    private Button voice_recognition_btn; // 음성 인식 버튼 추가

    // 현재 위치
    private NaverMap mNaverMap;
    private FusedLocationSource locationSource;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //네이버 지도
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 음성 인식 버튼 초기화 및 클릭 리스너 설정
        voice_recognition_btn = findViewById(R.id.voice_recognition_btn);
        voice_recognition_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 음성 인식 시작
                startVoiceRecognition();
            }
        });

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        /// 길찾기 탭으로 이동
        search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 검색어를 가져오기
                String searchQuery = ((EditText) findViewById(R.id.search_text)).getText().toString();
                // MapActivity로 검색어를 전달하는 Intent 생성
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("search_query", searchQuery); // "search_query"라는 이름으로 검색어를 전달
                startActivity(intent); // 액티비티 이동
            }
        });

        /// 환경 설정 탭으로 이동
        setting_btn = (Button) findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent); // 액티비티 이동
            }
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource);

        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

    }

    // 음성 인식 요청 코드
    private static final int SPEECH_REQUEST_CODE = 123;

    // 음성 인식 시작 함수
    private void startVoiceRecognition() {
        // 음성 인식 인텐트 생성
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 한국어 설정 (원하는 언어로 변경 가능)

        try {
            // 음성 인식 액티비티 시작
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // 음성 인식 앱이 설치되어 있지 않은 경우
            Toast.makeText(MainActivity.this, "음성 인식 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 음성 인식 결과 처리 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // 음성 인식 결과를 가져와서 search_text EditText에 설정
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String recognizedText = results.get(0); // 첫 번째 결과를 가져옴
                EditText searchEditText = findViewById(R.id.search_text);
                searchEditText.setText(recognizedText); // 검색어 설정
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한 획득 여부 확인
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }
}
