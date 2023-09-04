package com.example.open_sw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.location.Address;
import android.location.Geocoder;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private EditText searchEditText;
    private Button searchButton;
    private Button voice_recognition_btn; // 음성 인식 버튼 추가

    PreviewView previewView; // 카메라 프리뷰를 표시할 뷰
    String TAG = "MapActivity"; // 로그 태그
    ProcessCameraProvider processCameraProvider; // 카메라 프로바이더
    //int lensFacing = CameraSelector.LENS_FACING_FRONT;
    int lensFacing = CameraSelector.LENS_FACING_BACK; // 카메라 렌즈 방향 (후면 카메라로 설정)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 네이버 지도 초기화
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);


        voice_recognition_btn = findViewById(R.id.voice_recognition_btn);
        voice_recognition_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 음성 인식 액티비티를 시작합니다.
                startVoiceRecognition();
            }
        });


        // 검색어를 받아옴
        String searchQuery = getIntent().getStringExtra("search_query");

        // 검색어를 이용하여 지도에 해당 위치 표시
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchAndShowLocation(searchQuery);
        }

        // 검색 버튼 초기화 및 클릭 리스너 설정
        searchEditText = findViewById(R.id.search_text);
        searchButton = findViewById(R.id.search_btn);

        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                String location = searchEditText.getText().toString();
                if (!location.isEmpty()) {
                    // 검색어를 위도와 경도로 변환하고, 그 위치로 지도를 이동
                    searchAndShowLocation(location);
                }
            }
        });

        // 카메라 프리뷰를 표시할 뷰 찾기
        previewView = findViewById(R.id.cameraView);

        // 카메라 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        try {
            processCameraProvider = ProcessCameraProvider.getInstance(this).get();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 카메라 권한이 허용된 경우 카메라 프리뷰 설정
        if (ActivityCompat.checkSelfPermission(MapActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            bindPreview();
        }

    }

    // 음성 인식
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    private void startVoiceRecognition() {
        // 음성 인식 액티비티를 시작하고 결과를 받기 위한 인텐트 생성
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "음성을 말하세요.");

        // 음성 인식 액티비티 시작
        startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            // 음성 인식 결과를 가져옵니다.
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (matches != null && !matches.isEmpty()) {
                // 인식된 텍스트 중 첫 번째 결과를 가져와서 searchEditText에 설정합니다.
                String spokenText = matches.get(0);
                searchEditText.setText(spokenText);

                // 변환된 음성 인식 결과를 사용하여 지도 검색
                searchAndShowLocation(spokenText);
            }
        }
    }

    private void searchAndShowLocation(String location) {
        // Geocoder를 사용하여 주소를 위도와 경도로 변환
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                // 변환된 위도와 경도로 지도 이동
                LatLng target = new LatLng(latitude, longitude);
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(target);
                mapView.getMapAsync(naverMap -> naverMap.moveCamera(cameraUpdate));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    bindPreview();
                }
                else {
                    // 권한이 거부된 경우 처리
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void bindPreview() {

        // 원하는 넓이와 높이로 프리뷰 설정
        int yourWidth = 365; // 원하는 넓이
        int yourHeight = 280; // 원하는 높이

        // 프리뷰 크기 설정
        previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);

        // 카메라 렌즈 방향 설정
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        // 프리뷰 크기를 레이아웃에 딱 맞게 조정
        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(yourWidth, yourHeight))
                .build();


        preview.setSurfaceProvider(previewView.getSurfaceProvider()); // 프리뷰를 PreviewView에 연결

        // 카메라 라이프사이클에 바인딩
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview);

    }

    @Override
    protected void onPause() {
        super.onPause();
        processCameraProvider.unbindAll();  // 액티비티가 일시 중단될 때 카메라 사용 해제
    }


}
