package com.example.open_sw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

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
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;

import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPolyLine;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MapActivity extends AppCompatActivity  implements OnInitListener{


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

    private FirebaseStorage storage = FirebaseStorage.getInstance(); // 파이어베이스 저장소 객체
    private StorageReference reference = storage.getReference(); // 저장소 레퍼런스 객체 : storage 를 사용해 저장 위치를 설정

    //지도 변수 선언
    private LinearLayout linearLayoutTmap;
    private Context context;
    private TMapView tMapView;

    //검색창 관련 변수 선언
    private EditText inputStart_editText;
    private EditText inputEnd_editText;
    private ListView searchResult_listView;
    private List<String> list_data;
    private ArrayAdapter<String> adapter;
    private boolean display_listView = false;
    private boolean startLocation_finish = false;
    private Button findPath_btn;
    private Button change_btn;

    //TMap API 변수 선언
    private TMapData tMapData;

    //이동수단 경로 선택 변수 선언
    private Button select_car_btn;
    private Button select_bus_btn;
    private Button select_pedestrian_btn;
    private int findPath_case = 2; // default => 보행자


    //Marker 관련 변수 선언
    private Bitmap bitmap;
    private TMapPoint tMapPoint;
    private Marker startMarker;
    private Marker endMarker;

    //경로 안내 detail info 관련 변수 선언
    private Button detailInfo_path_btn;
    private Element root;

    //현위치 표시 관련 변수 선언
    private Button showCurPosition_btn;
    Bitmap bitmap2;
    TMapGpsManager tMapGpsManager;
    TMapPoint curPosition;

    // MainActivity 관련 변수 선언
    private String startName, startAddress, endName, endAddress;
    private double startLatitude, startLongitude, endLatitude, endLongitude;

    // tts 관련 변수 선언
    private TextToSpeech textToSpeech;
    private int currentDetailMessageIndex = 0;
    private String message_detail_string[] = new String[1000];
    private int pl=0;
    private Handler handler = new Handler();


    // 주기적으로 실행할 Runnable
    private final Runnable captureTask = new Runnable() {
        @Override
        public void run() {

            // takePicture() 메서드 호출 (여기서는 더미 메서드로 대체)
            takePicture();

        }
    };


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 이전 액티비티에서 데이터를 가져옴
        Intent intent = getIntent();
        startName = intent.getStringExtra("startName");
        startAddress = intent.getStringExtra("startAddress");
        startLatitude = intent.getDoubleExtra("startLatitude", 0.0);
        startLongitude = intent.getDoubleExtra("startLongitude", 0.0);
        endName = intent.getStringExtra("endName");
        endAddress = intent.getStringExtra("endAddress");
        endLatitude = intent.getDoubleExtra("endLatitude", 0.0);
        endLongitude = intent.getDoubleExtra("endLongitude", 0.0);
        findPath_case = intent.getIntExtra("findPathCase", 0);

        //-----------------------------field-------------------------------//
        //검색창 변수 설정
        inputStart_editText = (EditText) findViewById(R.id.inputStart_editText);
        inputEnd_editText = (EditText) findViewById(R.id.inputEnd_editText);
        searchResult_listView = (ListView)findViewById(R.id.searchResultList);
        list_data = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list_data);
        searchResult_listView.setAdapter(adapter);

        findPath_btn = (Button)findViewById(R.id.findPath_btn);
        change_btn = (Button)findViewById(R.id.change_btn);

        //지도 변수 설정
        linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        context = this;
        tMapView = new TMapView(context);

        //TMap API 변수 설정
        tMapData = new TMapData();

        //Marker 관련 변수 설정
        startMarker = new Marker();
        endMarker = new Marker();
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.poi_dot);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.poi_star);
        tMapPoint = new TMapPoint(0,0);

        //이동수단 경로 선택 변수 선언
        select_car_btn = (Button)findViewById(R.id.select_car_btn);
        select_bus_btn = (Button)findViewById(R.id.select_bus_btn);
        select_pedestrian_btn = (Button)findViewById(R.id.select_pedestrian_btn);
        select_pedestrian_btn.setSelected(true);

        //경로 안내 detail info 관련 변수 설정
        detailInfo_path_btn = (Button)findViewById(R.id.showDetailInfo_Path);
        detailInfo_path_btn.setVisibility(View.INVISIBLE);

        //현위치 표시 관련 변수 설정
        showCurPosition_btn = (Button)findViewById(R.id.showCurPosition_Btn);
        tMapGpsManager = new TMapGpsManager(context);
        tMapGpsManager.setMinTime(1000);
        tMapGpsManager.setMinDistance(5);
        tMapGpsManager.setProvider(tMapGpsManager.NETWORK_PROVIDER);

        // 음성인식
        voice_recognition_btn = findViewById(R.id.voice_recognition_btn);

        // tts
        textToSpeech = new TextToSpeech(this, this);


        //-----------------------method-----------------------//
        //-----------------------method-----------------------//


        //지도 설정
        context = this;
        tMapView = new TMapView(context);
        tMapView.setHttpsMode(true);
        tMapView.setSKTMapApiKey( "DWBbu4DTzLQhxCoLQ5u88vLX1nK6qPh65eKgkcn8" );
        linearLayoutTmap.addView( tMapView );


        // 음성 인식 이벤트 설정
        voice_recognition_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 음성 인식 액티비티를 시작합니다.
                startVoiceRecognition();
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

        Initial_Settings();
//        showMarkerPoint();

        //검색창 입력 이벤트 설정
        inputStart_editText.addTextChangedListener(new TextWatcher() {
            String input_locationPOI;

            //입력하기 전 이벤트
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //입력할 때  이벤트
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s == null) return;

                if(!display_listView){
                    startLocation_finish = false;
                    linearLayoutTmap.setVisibility(View.INVISIBLE);
                    searchResult_listView.setVisibility(View.VISIBLE);
                    display_listView = true;
                }

                //ListView 내용 초기화
                list_data.clear();

                if(s.toString().length() < 2){ //2개 미만은 검색 X
                    adapter.notifyDataSetChanged();
                    return;
                }

                input_locationPOI = s.toString();

                tMapData.findAllPOI(input_locationPOI, 7, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                        if(!arrayList.isEmpty()){
                            for(int i = 0; i < arrayList.size(); i++) {
                                TMapPOIItem item = arrayList.get(i);
                                list_data.add(item.getPOIName());

                            }
                            //새로운 Thread 생성
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable(){
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                        }
                    }

                });


            }

            //입력 후 이벤트
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputEnd_editText.addTextChangedListener(new TextWatcher() {
            String input_locationPOI;

            //입력하기 전 이벤트
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //입력할 때  이벤트
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s == null) return;

                //ListView 내용 초기화
                list_data.clear();
                if(!display_listView){
                    startLocation_finish = true;
                    linearLayoutTmap.setVisibility(View.INVISIBLE);
                    searchResult_listView.setVisibility(View.VISIBLE);
                    display_listView = true;
                }




                if(s.toString().length() < 2){ //2개 미만은 검색 X
                    adapter.notifyDataSetChanged();
                    return;
                }


                input_locationPOI = s.toString();

                tMapData.findAllPOI(input_locationPOI, 7, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                        if(!arrayList.isEmpty()){
                            for(int i = 0; i < arrayList.size(); i++) {
                                TMapPOIItem item = arrayList.get(i);
                                list_data.add(item.getPOIName());

                            }
                            //새로운 Thread 생성
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable(){
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                        }
                    }

                });


            }

            //입력 후 이벤트
            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        //검색창 클릭 이벤트 설정
        searchResult_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            int index = 0;
            Marker marker;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tMapData.findAllPOI(list_data.get(position), 1, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                        TMapPOIItem item;
                        item = arrayList.get(0);
                        marker = new Marker(item.getPOIName(), item.getPOIAddress(), "None",item.getPOIPoint().getLatitude(), item.getPOIPoint().getLongitude());


                        if (!startLocation_finish) {
                            inputStart_editText.setText(marker.getName());
                            startMarker = marker;
                        } else {
                            inputEnd_editText.setText(marker.getName());
                            endMarker = marker;
                        }


                        //카메라 이동
                        tMapView.setCenterPoint(marker.getLongitude(),marker.getLatitude());

                        //Point 좌표 설정
                        tMapPoint.setLongitude(marker.getLongitude());
                        tMapPoint.setLatitude(marker.getLatitude());

                        TMapMarkerItem tMapMarkerItem = new TMapMarkerItem();


                        tMapMarkerItem.setIcon(bitmap); // 마커 아이콘 지정
                        tMapMarkerItem.setCanShowCallout(true);
                        tMapMarkerItem.setAutoCalloutVisible(true);
                        tMapMarkerItem.setTMapPoint(tMapPoint); //마커 좌표 설정

                        //마커 Title & SubTitle 지정
                        tMapMarkerItem.setCalloutTitle(marker.getName());
                        tMapMarkerItem.setCalloutSubTitle(marker.getAddress().trim());

                        if(!startLocation_finish){
                            startMarker.setMarker_id("markerItem_1");
                            tMapView.addMarkerItem(startMarker.getMarker_id(), tMapMarkerItem); // 지도에 마커 추가
                        }
                        else{
                            endMarker.setMarker_id("markerItem_2");
                            tMapView.addMarkerItem(endMarker.getMarker_id(), tMapMarkerItem); // 지도에 마커 추가
                        }


                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        // 해당 작업을 처리함
                                        linearLayoutTmap.setVisibility(View.VISIBLE);
                                        searchResult_listView.setVisibility(View.INVISIBLE);
                                        display_listView = false;
                                    }
                                });
                            }
                        }).start();


                    }
                });



            }

        });

        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //endMaker & startMarker 정보 swap
                Marker marker_instance = startMarker;
                startMarker = endMarker;
                endMarker = marker_instance;

                //editText text 내용 swap
                inputStart_editText.setText(startMarker.getName());
                inputEnd_editText.setText(endMarker.getName());

                //listView 안 보이게 설정
                startLocation_finish = true;
                linearLayoutTmap.setVisibility(View.VISIBLE);
                searchResult_listView.setVisibility(View.INVISIBLE);
                display_listView = false;


            }
        });

        findPath_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //카메라 중심정 이동
                TMapPoint tMapPoint_instance = new TMapPoint((startMarker.getLatitude() + endMarker.getLatitude())/2 ,
                        (startMarker.getLongitude() + endMarker.getLongitude())/2);
                tMapView.setCenterPoint(tMapPoint_instance.getLongitude(),tMapPoint_instance.getLatitude(),true);
                tMapView.setZoomLevel(14);


                //경로 안내
                switch (findPath_case){
                    case 0:     //자동차
//                        FindPath_Car();
                        break;
                    case 1:     //대중교통
                        FindPath_Bus();
                        break;
                    case 2:     //보행자
                        FindPath_Pedestrian();
                        break;
                }
            }
        });

        //이동수단 버튼 클릭 이벤트 설정
        //대중교통
        select_bus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_bus_btn.setSelected(true); //선택한 버튼 클릭 상태로 유지
                SelectTransPort(1); //그 전에 선택한 버튼 클릭 상태 해제 및 findPath_case 값 변경
            }
        });
        //보행자
        select_pedestrian_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_pedestrian_btn.setSelected(true); //선택한 버튼 클릭 상태로 유지
                SelectTransPort(2); //그 전에 선택한 버튼 클릭 상태 해제 및 findPath_case 값 변경
            }
        });

        //경로 안내 자세한 정보 버튼 클릭
        detailInfo_path_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalDistance = 0; //총거리
                int crosswalk_number = 0; //횡단보도 개수
                int overpass_number = 0; // 육교 개수
                int undergroundWalkway_number = 0; //지하보도 개수
                int tunnel_number = 0; //터널 개수
                int number = 0; //임시 숫자
                String message_simple = ""; //실제 경로 안내 간단한 형태
                String message_detail = "<===== 자세한 정보 =====>\n"; //실제 경로 안내 자세한 형태


                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");

                for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();

                    for( int j=0; j<nodeListPlacemarkItem.getLength(); j++ ) {
                        String str = "";
                        int index = 0;
                        System.out.println(nodeListPlacemarkItem.item(j).getNodeName());
                        if( nodeListPlacemarkItem.item(j).getNodeName().equals("description") ) {
                            str = nodeListPlacemarkItem.item(j).getTextContent().trim();
                            index = str.indexOf(",");
                            if(index == -1){
                                message_detail += str + "\n";
                                message_detail_string[pl++] = str;
                            }
                        }
                        else if(nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:facilityType")){
                            System.out.println(nodeListPlacemarkItem.item(j).getTextContent().trim());
                            if(nodeListPlacemarkItem.item(j).getTextContent().trim() != ""){
                                number = Integer.parseInt(nodeListPlacemarkItem.item(j).getTextContent().trim());
                                switch (number){
                                    case 1: //교량
                                        break;
                                    case 2: //터널
                                        tunnel_number++;
                                        break;
                                    case 3: //고가도로
                                        break;
                                    case 11: //일반보행자도로
                                        break;
                                    case 12: //육교
                                        overpass_number++;
                                        break;
                                    case 14: //지하보도
                                        undergroundWalkway_number++;
                                        break;
                                    case 15: //횡단보도
                                        crosswalk_number++;
                                        break;
                                    case 16: //대형시설물이동통로
                                        break;
                                    case 17: //계단
                                        break;
                                }
                            }



                        }
                        else if(nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:distance")){
                            totalDistance += Integer.parseInt(nodeListPlacemarkItem.item(j).getTextContent().trim());
                        }
                    }
                }
                message_simple = "총 거리 : " + totalDistance/2 + "m" + "\n" +
                        "횡단보도 개수 : " + crosswalk_number/2 + "\n" +
                        "육교 개수 : " + overpass_number/2 + "\n" +
                        "지하보도 개수 : " + undergroundWalkway_number/2 + "\n" +
                        "터널 개수 : " + tunnel_number/2 + "\n";

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("출발 : " + startMarker.getName() + "\n" + "도착 : " + endMarker.getName());
                builder.setMessage(message_simple + message_detail);

                AlertDialog alertDialog = builder.create();

                alertDialog.show();

                readAllDetailMessages();
            }
        });

        //현위치 표시 버튼 클릭 이벤트
        showCurPosition_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //gps.setProvider(gps.GPS_PROVIDER); // 핸드폰일때?? 잘 모르겠음

                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
                    }
                    return;
                }

                tMapGpsManager.OpenGps();

                //현위치 잡기
                curPosition = new TMapPoint(tMapGpsManager.getLocation().getLatitude(), tMapGpsManager.getLocation().getLongitude());

                //현위치 마커 설정
                TMapMarkerItem tMapMarkerItem = new TMapMarkerItem();


                tMapMarkerItem.setIcon(bitmap2); // 마커 아이콘 지정
                tMapMarkerItem.setCanShowCallout(true);
                tMapMarkerItem.setAutoCalloutVisible(true);
                tMapMarkerItem.setTMapPoint(curPosition); //마커 좌표 설정

                //마커 Title & SubTitle 지정
                tMapMarkerItem.setCalloutTitle("현위치");

                //현위치 마커 표시
                tMapView.addMarkerItem("marker_curPosition",tMapMarkerItem);


                //현위치로 카메라 이동
                tMapView.setCenterPoint(curPosition.getLongitude(),curPosition.getLatitude(),false);


                System.out.println(curPosition.getLatitude() + " " + curPosition.getLongitude());


            }
        });


        // 카메라 중심점 이동
        tMapView.setCenterPoint(startMarker.getLongitude(), startMarker.getLatitude());

    }


    protected void Initial_Settings() {

        Toast.makeText(MapActivity.this, startName, Toast.LENGTH_SHORT).show();

        inputStart_editText.setText(startName);
        startMarker = new Marker(startName, startAddress, "None", startLatitude, startLongitude);

        inputEnd_editText.setText(endName);
        endMarker = new Marker(endName, endAddress, "None", endLatitude, endLongitude);

        //detail Info 버튼 활성화
        detailInfo_path_btn.setVisibility(View.VISIBLE);


        //경로 안내
        switch (findPath_case){
            case 0:     //자동차
//                        FindPath_Car();
                break;
            case 1:     //대중교통
                FindPath_Bus();
                break;
            case 2:     //보행자
                FindPath_Pedestrian();
                break;
        }

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
                inputEnd_editText.setText(spokenText);

                // 변환된 음성 인식 결과를 사용하여 지도 검색
                FindPath_Pedestrian();
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

    //보행자 경로 찾는 함수
    public void FindPath_Pedestrian(){
        TMapPoint startpoint = new TMapPoint(startMarker.getLatitude(),startMarker.getLongitude());
        TMapPoint endpoint = new TMapPoint(endMarker.getLatitude(),endMarker.getLongitude());


        tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView.addTMapPath(tMapPolyLine);
            }
        });

        tMapData.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint, new TMapData.FindPathDataAllListenerCallback() {
                    @Override
                    public void onFindPathDataAll(Document document) {
                        root = document.getDocumentElement();
                    }
                }
        );
    }

    public void FindPath_Bus(){
        TMapPoint startpoint = new TMapPoint(startMarker.getLatitude(),startMarker.getLongitude());
        TMapPoint endpoint = new TMapPoint(endMarker.getLatitude(),endMarker.getLongitude());


        tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapView.addTMapPath(tMapPolyLine);
            }
        });

        tMapData.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint, new TMapData.FindPathDataAllListenerCallback() {
                    @Override
                    public void onFindPathDataAll(Document document) {
                        Element root = document.getDocumentElement();
                        NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");

                        for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
                            NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();

                            for( int j=0; j<nodeListPlacemarkItem.getLength(); j++ ) {
                                if( nodeListPlacemarkItem.item(j).getNodeName().equals("description") ) {
                                    Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim() );
                                }
                            }
                        }
                    }
                }
        );
    }

    //이동수단 버튼 클릭 이벤트 관련 함수
    public void SelectTransPort(int value){
        if(findPath_case == value) return;
        switch (findPath_case){
            case 0:
                select_car_btn.setSelected(false);
                break;
            case 1:
                select_bus_btn.setSelected(false);
                break;
            case 2:
                select_pedestrian_btn.setSelected(false);
                break;
        }

        findPath_case = value;
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
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.KOREAN); // TTS 언어 설정 (예: 영어)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 언어 데이터가 누락되었거나 지원되지 않는 경우 처리
                Log.e("TTS", "Language is not supported.");
            } else {
                // TTS 초기화 성공
                // 이제 상세 경로 읽기를 시작할 수 있습니다.
                // readNextDetailMessage();
                readAllDetailMessages();
            }
        } else {
            // TTS 초기화 실패 처리
            Log.e("TTS", "Initialization failed.");
        }
    }

    // 모든 상세 메시지 읽기
    private void readAllDetailMessages() {
        Pattern pattern = Pattern.compile("\\d+"); // 정규 표현식: 하나 이상의 숫자
        Handler handler = new Handler();
        int delay = 0; // 딜레이 시간 (2초)

        for (int i = 0; i < pl; i++) {
            String message = message_detail_string[i];
            // 정규 표현식과 일치하는 숫자 추출
            Matcher matcher = pattern.matcher(message);
            int num = -1; // 숫자를 저장할 변수, 초기값은 -1

            if (matcher.find()) {
                String numberStr = matcher.group(); // 일치하는 숫자 부분 추출
                num = Integer.parseInt(numberStr); // 추출한 숫자 문자열을 정수로 변환
            }

            String Check = message;
//            if (num != -1) {
//                Check += num;
//                Toast.makeText(this, Check, Toast.LENGTH_SHORT).show();
//            }

            // 현재 문장을 TTS로 읽고, delay 밀리초 후 다음 문장으로 넘어감
            final int currentIndex = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textToSpeech.speak(message_detail_string[currentIndex], TextToSpeech.QUEUE_ADD, null, null);

                    // 마지막 문장이면 도착 메시지를 추가로 읽음
                    if (currentIndex == pl - 1) {
                        String arrivalMessage = "도착했습니다. 안내를 종료합니다.";
                        textToSpeech.speak(arrivalMessage, TextToSpeech.QUEUE_ADD, null, null);
                    }
                }
            }, delay); // 딜레이 시간을 현재 인덱스에 따라 증가시킴


            delay += num * 100;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        processCameraProvider.unbindAll();  // 액티비티가 일시 중단될 때 카메라 사용 해제
    }

    // 액티비티가 종료될 때 TTS를 정리
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();

        executor.shutdown();
    }

}
