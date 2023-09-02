package com.example.open_sw;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MapActivity extends AppCompatActivity {

    private Button search_btn;
    private Button setting_btn;
    private Button speaker_btn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    }
}