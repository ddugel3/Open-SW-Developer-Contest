package com.example.open_sw;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingActivity extends AppCompatActivity {

    private Button user_btn;
    private Button accessibility_btn;
    private Button language_btn;
    private Button security_btn;
    private Button information_btn;
    private Button feedback_btn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        /// 개인 정보 탭으로 이동
        user_btn = (Button)findViewById(R.id.user_btn);
        user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, PersonalActivity.class);
                startActivity(intent); // 액티비티 이동
            }
        });

        /// 접근성 탭으로 이동
//        accessibility_btn = (Button) findViewById(R.id.accessibility_btn);
//        accessibility_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                /// 접근성
//            }
//        });


        /// 언어 설정 탭으로 이동
//        language_btn = (Button) findViewById(R.id.language_btn);
//        language_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 언어
//            }
//        });

        /// 보안 탭으로 이동
//        security_btn = (Button) findViewById(R.id.security_btn);
//        security_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 보안
//            }
//        });

        /// 정보 탭으로 이동
//        information_btn = (Button) findViewById(R.id.information_btn);
//        information_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 정보
//            }
//        });

        /// 피드백 탭으로 이동
        feedback_btn = (Button) findViewById(R.id.feedback_btn);
        feedback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, FeedbackActivity.class);
                startActivity(intent); // 액티비티 이동
            }
        });

    }
}