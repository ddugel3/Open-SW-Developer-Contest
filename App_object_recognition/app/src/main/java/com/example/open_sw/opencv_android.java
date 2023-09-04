package com.example.open_sw;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class opencv_android extends AppCompatActivity implements View.OnClickListener {
    final int CAMERA = 100; // 카메라 선택시 인텐트로 보내는 값
    final int GALLERY = 101; // 갤러리 선택 시 인텐트로 보내는 값
    int imgFrom; // 이미지 어디서 가져왔는지 (카메라 or 갤러리)
    String imagePath = "";
    String TAG = "@@TAG@@";
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat imageDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
    Intent intent;

    ImageView imageView;
    Button btnCamera, btnGallery, btnMove, btnUpload;

    ProgressDialog mProgressDialog;

    File imageFile = null; // 카메라 선택 시 새로 생성하는 파일 객체
    Uri imageUri = null;


    FirebaseStorage storage = FirebaseStorage.getInstance(); // 파이어베이스 저장소 객체
    StorageReference reference = null; // 저장소 레퍼런스 객체 : storage 를 사용해 저장 위치를 설정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv_android);

        imageView = findViewById(R.id.iv_main);
        btnCamera = findViewById(R.id.btn_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnMove = findViewById(R.id.btn_move);
        btnUpload = findViewById(R.id.btn_upload);

        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnMove.setOnClickListener(this);
        btnUpload.setOnClickListener(this);



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
                Log.d(TAG, "Value is: " + value);
                Toast.makeText(opencv_android.this, "Value is: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // onCancelled 메서드는 데이터 읽기 실패 또는 취소될 때 호출됩니다.
                // 에러를 로그에 기록합니다.
                Log.w(TAG, "Failed to read value.", error.toException());
                Toast.makeText(opencv_android.this, "failed", Toast.LENGTH_SHORT).show();
            }
        });


        //        권한 체크
        boolean hasCamPerm = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasCamPerm = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        boolean hasWritePerm = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasWritePerm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        if (!hasCamPerm || !hasWritePerm)  // 권한 없을 시  권한설정 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @SuppressLint({"NonConstantResourceId", "QueryPermissionsNeeded"})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera: // 카메라 선택 시
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    try {
                        imageFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (imageFile != null) {
                        Uri imageUri = FileProvider.getUriForFile(getApplicationContext(),
                                "com.example.uploadimage.fileprovider",
                                imageFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent, CAMERA);
                    }
                }
                break;
            case R.id.btn_gallery: // 갤러리 선택 시
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY);
                break;
            case R.id.btn_move: // 이동 선택 시
                if (imagePath.length() > 0) { // 이미지 경로가 있을 경우
                    intent = new Intent(getApplicationContext(), setimage.class);
                    intent.putExtra("path", imagePath);
                    startActivity(intent);
                }
                break;
            case R.id.btn_upload: // 업로드 선택 시
                if (imagePath.length() > 0 && imgFrom >= 100) {
                    uploadImg(); // 업로드 작업 실행
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) { // 결과가 있을 경우
//            갤러리를 선택한 경우 인텐트를 활용해 이미지 정보 가져오기
            if (requestCode == GALLERY) { // 갤러리 선택한 경우
                imageUri = data.getData(); // 이미지 Uri 정보
                imagePath = data.getDataString(); // 이미지 위치 경로 정보
            }
          /*  카메라를 선택할 경우, createImageFile()에서 별도의 imageFile 을 생성 및 파일 절대경로 저장을 하기 때문에
            onActivityResult()에서는 별도의 작업 필요無 */

//            저장한 파일 경로를 이미지 라이브러리인 Glide 사용하여 이미지 뷰에 세팅하기
            if (imagePath.length() > 0) {
                Glide.with(this)
                        .load(imagePath)
                        .into(imageView);
                imgFrom = requestCode; // 사진을 가져온 곳이 카메라일 경우 CAMERA(100), 갤러리일 경우 GALLERY(101)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    File createImageFile() throws IOException {
//        이미지 파일 생성
        String timeStamp = imageDate.format(new Date()); // 파일명 중복을 피하기 위한 "yyyyMMdd_HHmmss"꼴의 timeStamp
        String fileName = "IMAGE_" + timeStamp; // 이미지 파일 명
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(fileName,
                ".jpg",
                storageDir); // 이미지 파일 생성
        imagePath = file.getAbsolutePath(); // 파일 절대경로 저장하기
        return file;
    }

    void uploadImg() {
//        firebase storage 에 이미지 업로드하는 method
        showProgressDialog("업로드 중");
        UploadTask uploadTask = null; // 파일 업로드하는 객체
        switch (imgFrom) {
            case GALLERY:
                /*갤러리 선택 시 새로운 파일명 생성 후 reference 에 경로 세팅,
                 * uploadTask 에서 onActivityResult()에서 받은 인텐트의 데이터(Uri)를 업로드하기로 설정*/
                String imageFileName = "image.png"; // 파일명
                reference = storage.getReference().child("item").child(imageFileName); // 이미지 파일 경로 지정 (/item/imageFileName)
                uploadTask = reference.putFile(imageUri); // 업로드할 파일과 업로드할 위치 설정
                break;
            case CAMERA:
                /*카메라 선택 시 생성했던 이미지파일명으로 reference 에 경로 세팅,
                 * uploadTask 에서 생성한 이미지파일을 업로드하기로 설정*/
                reference = storage.getReference().child("item").child(imageFile.getName()); // imageFile.toString()을 할 경우 해당 파일의 경로 자체가 불러와짐
                uploadTask = reference.putFile(Uri.fromFile(imageFile)); // 업로드할 파일과 업로드할 위치 설정
                break;
        }

//        파일 업로드 시작
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//            업로드 성공 시 동작
                hideProgressDialog();
                Log.d(TAG, "onSuccess: upload");
                downloadUri(); // 업로드 성공 시 업로드한 파일 Uri 다운받기
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                업로드 실패 시 동작
                hideProgressDialog();
                Log.d(TAG, "onFailure: upload");
            }
        });
    }

    void downloadUri() {
//        지정한 경로(reference)에 대한 uri 을 다운로드하는 method
        showProgressDialog("다운로드 중");
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
//                uri 다운로드 성공 시 동작
//                다운받은 uri를 인텐트에 넣어 다른 액티비티로 이동
                hideProgressDialog();
                Log.d(TAG, "onSuccess: download");
                intent = new Intent(opencv_android.this, setimage.class);
                intent.putExtra("path", uri.toString()); // 다운로드한 uri, String 형으로 바꿔 인텐트에 넣기
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                uri 다운로드 실패 시 동작
                hideProgressDialog();
                Log.d(TAG, "onFailure: download");
            }
        });
    }

    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}