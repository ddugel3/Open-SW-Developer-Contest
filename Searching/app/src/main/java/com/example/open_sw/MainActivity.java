package com.example.open_sw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.EditText;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.MapView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private static NaverMap naverMap;
    private Button search_btn;
    private Button setting_btn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //네이버 지도
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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

    }
}