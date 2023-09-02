package com.example.open_sw;

import static com.google.android.material.internal.ViewUtils.dpToPx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;

import android.content.pm.PackageManager;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import java.util.concurrent.ExecutionException;


public class MapActivity extends AppCompatActivity {

    private Button search_btn;
    private Button setting_btn;
    private Button speaker_btn;

    PreviewView previewView; // 카메라 프리뷰를 표시할 뷰
    String TAG = "MapActivity"; // 로그 태그
    ProcessCameraProvider processCameraProvider; // 카메라 프로바이더
    //int lensFacing = CameraSelector.LENS_FACING_FRONT;
    int lensFacing = CameraSelector.LENS_FACING_BACK; // 카메라 렌즈 방향 (후면 카메라로 설정)


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        /// 길찾기 재시작 버튼
//        search_btn = (Button)findViewById(R.id.search_btn);
//        search_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 길찾기 재시작
//            }
//        });

        /// 환경 설정 탭으로 이동
        setting_btn = (Button) findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this, SettingActivity.class);
                startActivity(intent); // 액티비티 이동
            }
        });


        /// 음소거 버튼
//        speaker_btn = (Button) findViewById(R.id.speaker_btn);
//        search_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 음소거
//            }
//        });

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