package com.example.open_sw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.internal.utils.ImageUtil;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.location.Address;
import android.location.Geocoder;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private EditText searchEditText;
    private Button searchButton;
    private Button voice_recognition_btn; // 음성 인식 버튼 추가

    private PreviewView previewView; // 카메라 프리뷰를 표시할 뷰
    private ProcessCameraProvider processCameraProvider; // 카메라 프로바이더
    //int lensFacing = CameraSelector.LENS_FACING_FRONT;
    private int lensFacing = CameraSelector.LENS_FACING_BACK; // 카메라 렌즈 방향 (후면 카메라로 설정)

    // 이미지 촬영 간격을 조절하기 위한 변수
    private static final long CAPTURE_INTERVAL = 5000; // 1초마다 촬영
    ImageCapture imageCapture;


    // 이미지 촬영을 주기적으로 수행할 ExecutorService
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private Executor executor_image = Executors.newSingleThreadExecutor();

    FirebaseStorage storage = FirebaseStorage.getInstance(); // 파이어베이스 저장소 객체
    StorageReference reference = storage.getReference(); // 저장소 레퍼런스 객체 : storage 를 사용해 저장 위치를 설정



    // 주기적으로 실행할 Runnable
    private final Runnable captureTask = new Runnable() {
        @Override
        public void run() {

            // takePicture() 메서드 호출 (여기서는 더미 메서드로 대체)
            takePicture();

        }
    };


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

        // firebase
        // Firebase 초기화
        FirebaseApp.initializeApp(this);

        // firebase database
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("object_recognition_results");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // onDataChange 메서드는 데이터가 변경될 때 호출됩니다.
                // 데이터 스냅샷에서 값을 가져와서 String으로 변환합니다.
                String value = dataSnapshot.child("message").getValue(String.class);



                // 변경된 값을 로그에 출력합니다.
                Log.d("MapActivity", "Value is: " + value);
                Toast.makeText(MapActivity.this, "Value is: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // onCancelled 메서드는 데이터 읽기 실패 또는 취소될 때 호출됩니다.
                // 에러를 로그에 기록합니다.
                Log.w("MapActivity", "Failed to read value.", error.toException());
                Toast.makeText(MapActivity.this, "failed", Toast.LENGTH_SHORT).show();
            }
        });

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

        // 음성 인식
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    bindPreview();
                    bindImageCapture();
                    executor.scheduleAtFixedRate(captureTask, 3000, CAPTURE_INTERVAL, TimeUnit.MILLISECONDS);
                }
                else {
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    void bindImageCapture() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        // ImageCapture 초기화 및 설정
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(Surface.ROTATION_270)
                .build();

        // 카메라와 ImageCapture를 라이프사이클에 바인딩
        processCameraProvider.bindToLifecycle(this, cameraSelector, imageCapture);
    }

    public void takePicture() {

        imageCapture.takePicture(ContextCompat.getMainExecutor(MapActivity.this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {

                        // 이미지 뷰가 null이 아닌 경우에만 setImageBitmap을 호출합니다.
                        Bitmap bitmap = imageProxyToBitmap(image); // 이미지를 Bitmap으로 변환하는 함수 호출
                        bitmap = rotateBitmap(bitmap, 90);
                        uploadImg(bitmap);

                        super.onCaptureSuccess(image);
                    }
                }
        );
    }

    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    void uploadImg(Bitmap bitmap) {
        // Firebase Storage에 업로드할 경로 설정 (원하는 경로로 변경)
        StorageReference imageRef = reference.child("item/image.jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // 바이트 배열 업로드
        UploadTask uploadTask = imageRef.putBytes(imageData);

        // 업로드 리스너 설정
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 업로드 성공 시 처리
                Log.d("FirebaseStorage", "Image uploaded successfully!");
                // 업로드된 이미지의 다운로드 URL을 가져와 원하는 처리 수행
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        // 여기에서 다운로드 URL을 사용하여 필요한 작업을 수행할 수 있습니다.
                        // 예: 데이터베이스에 URL 저장 또는 이미지를 표시
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 업로드 실패 시 처리
                Log.e("FirebaseStorage", "Image upload failed: " + e.getMessage());
            }
        });
    }

    public Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image == null) {
            return null;
        }

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        BitmapFactory.Options options = new BitmapFactory.Options();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            options.outConfig = Bitmap.Config.ARGB_8888; // 원하는 Bitmap 구성을 선택합니다.
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        imageProxy.close(); // ImageProxy를 사용한 후에는 반드시 닫아주어야 합니다.

        return bitmap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        processCameraProvider.unbindAll();  // 액티비티가 일시 중단될 때 카메라 사용 해제
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 종료될 때 스케줄된 작업을 종료합니다.
        executor.shutdown();
    }

}
