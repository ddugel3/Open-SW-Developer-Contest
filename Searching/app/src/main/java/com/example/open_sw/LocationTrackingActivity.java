//package com.example.open_sw;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.fragment.app.FragmentManager;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//
//import com.google.android.gms.maps.GoogleMap;
//import com.naver.maps.map.LocationTrackingMode;
//import com.naver.maps.map.MapFragment;
//import com.naver.maps.map.NaverMap;
//import com.naver.maps.map.OnMapReadyCallback;
//import com.naver.maps.map.overlay.Marker;
//import com.naver.maps.map.util.FusedLocationSource;
//
//public class LocationTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    private FusedLocationSource locationSource;
//    private NaverMap mNaverMap;
//
//    private static final int PERMISSION_REQUEST_CODE = 100;
//    private static final String[] PERMISSIONS = {
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_map);
//
//        //지도 객체 생성하기
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_view);
//        if(mapFragment == null){
//            mapFragment = MapFragment.newInstance();
//            fragmentManager.beginTransaction().add(R.id.map_view, mapFragment).commit();
//
//        }
//
//        //getMapAsync 호출해 비동기로 onMapReady 콜백 메서드 호출
//        //onMapReady에서 NaverMap 객체를 받음.
//        mapFragment.getMapAsync(this);
//
//        //위치를 반환하는 구현체인 FusedLocationSource 생성
//        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
//
//
//    }
//
//
//    @Override
//    public void onMapReady(@NonNull NaverMap naverMap) {
//
//
//        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
//        mNaverMap = naverMap;
//        mNaverMap.setLocationSource(locationSource);
//
//        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
//        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        // request code와 권한 획득 여부 확인
//        if(requestCode == PERMISSION_REQUEST_CODE){
//            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
//            }
//        }
//    }
//}