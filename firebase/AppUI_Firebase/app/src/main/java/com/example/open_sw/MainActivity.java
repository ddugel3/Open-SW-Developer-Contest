package com.example.open_sw;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    // 파이어베이스
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getInstance().getReference().child("test"); //db location

    EditText inputText;

    private Button search_btn;
    private Button setting_btn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // edit text
        inputText = (EditText) findViewById((R.id.search_text));

        /// 길찾기 탭으로 이동
        search_btn = (Button)findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addInputText(inputText.getText().toString());

                Intent intent = new Intent(MainActivity.this, MapActivity.class);
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
    public void addInputText(String inputText){
        firebase tt = new firebase(inputText);
        databaseReference.setValue((tt.getInputText()));
    }
}