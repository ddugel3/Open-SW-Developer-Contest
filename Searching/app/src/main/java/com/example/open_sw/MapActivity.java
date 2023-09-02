package com.example.open_sw;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.location.Address;
import android.location.Geocoder;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private EditText searchEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 네이버 지도 초기화
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

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
}
