package com.example.notepad2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteActivity extends AppCompatActivity {


    private int dialogCount;//다이얼로그 중첩 방지 카운트

    private ImageButton back,share,delete,save;
    private TextView date_time;
    private EditText contents;
    private AlertDialog.Builder alBuilder;
    private SharedPreferences sf;
    private Switch sw;

    //날짜 가져오기------------
    private long mNow;
    private Date mDate;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd aa hh:mm");
    //------------------------


    // 가속도 센서 등록---------------------------------------
    private SensorManager mSensorManager;
    private Sensor mAccelermeter;
    private SensorEventListener accL;//센서 이벤트 리스너
    private long mshakeTime;
    private static final int SHAKE_SKIP_TIME=2000;//2초
    private static final float SHAKE_THRESHOLD_GRAVITY = 3.5F;
    //---------------------------------------------------------



    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }//시간 받아오기 함수



    protected class accListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {  // 가속도 센서 값이 바뀔때마다 호출됨

            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                float gravityX = axisX / SensorManager.GRAVITY_EARTH;
                float gravityY = axisY / SensorManager.GRAVITY_EARTH;
                float gravityZ = axisZ / SensorManager.GRAVITY_EARTH;

                Float f = gravityX*gravityX + gravityY*gravityY + gravityZ*gravityZ;
                double squaredD = Math.sqrt(f.doubleValue());
                float gForce = (float)squaredD;

                if(gForce>SHAKE_THRESHOLD_GRAVITY){
                    long currentTime = System.currentTimeMillis();
                    if(mshakeTime+SHAKE_SKIP_TIME>currentTime){
                        return;
                    }//현재 시간을 저장후 skip_time 만큼의 시간동안은 shake 조건을 주지 않는다.
                    mshakeTime=currentTime;

                    contents.setText("");

                }// 흔들림이 감지된 경우 내용 지우기
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        sw = (Switch)findViewById(R.id.switch1);
        date_time = (TextView)findViewById(R.id.date_time);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//센서 매니저 인스턴스 가져오고
        mAccelermeter = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//사용할 가속도 센서 가져오고
        accL = new accListener();//센서 리스너
        //가속도 센서 등록부분

        back = (ImageButton)findViewById(R.id.back);
        save = (ImageButton)findViewById(R.id.save);
        share = (ImageButton)findViewById(R.id.share);
        delete = (ImageButton)findViewById(R.id.delete);
        contents = (EditText)findViewById(R.id.new_contents);

        //위젯 아이디 할당


        //다이얼로그 선언
        alBuilder= new AlertDialog.Builder(this);
        alBuilder.setMessage("이어하시겠습니까?");
        alBuilder.setCancelable(false);

        alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // "예" 버튼을 누르면 실행되는 리스너
                dialogCount=0;

                //생명주기는 다이얼로그로 흐름을 제어할 수 없기 때문에
                //다이얼로그가 실행된다 하여도 멈추지 않고 onResume까지 흐르게 되어
                //예를 누른다고 하더라도 이미 데이터는 onStart에서 복구되어있기 때문에
                //예를 누를때는 아무 작업이 필요 없음
                //이어서 저장된 것 처럼 보이게 하는 것.
            }
        });
        alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // "아니오" 버튼을 누르면 실행되는 리스너

                contents.setText("");
                dialogCount=0;

                //여기서 지워주지 않는다면 다음에 켰을때 sf에 저장된 정보를 가지고 오기 때문에
                //반드시 지워줘야한다.

                //이어서 저장하시겠습니까에서 아니요를 눌렀을때에는
                //그저 텍스트만 초기화 해주면 이어하지 않는 다는 의미로 전달이 된다.
            }
        });

        alBuilder.setTitle("이어서작성하기");


        sf = getSharedPreferences("sFile",MODE_PRIVATE);//저장소 선언


        //버튼과 텍스트 할당
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();// 앱이 pause->stop->delete 순으로 종료된다.
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getIntent().getBooleanExtra("add_check",false)){
                Intent intent = new Intent(NoteActivity.this,MainActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, contents.getText().toString());
                intent.putExtra("date",date_time.getText().toString() );
                setResult(RESULT_OK,intent);
                finish();
                }
                else if(getIntent().getBooleanExtra("update_check",false)){
                    Intent intent = new Intent(NoteActivity.this,MainActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, contents.getText().toString());
                    intent.putExtra("date",date_time.getText().toString() );
                    intent.putExtra("item_position",getIntent().getIntExtra("item_position",0));
                    setResult(RESULT_OK,intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(NoteActivity.this,MainActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, contents.getText().toString());
                    intent.putExtra("date",date_time.getText().toString() );
                    intent.putExtra("from_external_check",true);
                    startActivity(intent);
                    finish();
                }

                //add버튼을 통해 액티비티가 실행된거면 Result를 통해 돌려주고
                //그 외는 무조건 메인 액티비티로 데이터를 보내려면 startActivity 형식을 취해야한다.

            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, contents.getText().toString());

                Intent chooser = Intent.createChooser(intent, "공유");
                startActivity(chooser);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contents.setText("");
            }
        });



        date_time.setText(getTime());
        //새로운 메모시에만 시간 가져오기


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(dialogCount==0){
            alBuilder.show();
            dialogCount++;
        }//dialog 중첩실행방지
        //액티비티가 stop이 되는 상태에서 돌아올때만 조건을 주고 싶어서 restart에 넣었다,
        //start에 넣으면 첫 시작에도 다이얼로그를 띄우기 때문에 부적절하다고 판단했다.

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(sf!=null) {
            Boolean chk = sf.getBoolean("switchCheck",false);
            sw.setChecked(chk);
        }

        if(getIntent().getBooleanExtra("update_check",false)){
            date_time.setText(getIntent().getStringExtra("main_date"));
            contents.setText(getIntent().getStringExtra("main_contents"));
        }//인텐트 받기



        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(NoteActivity.this, "체크상태 = " + isChecked, Toast.LENGTH_SHORT).show();

                if(sw.isChecked()){
                    mSensorManager.unregisterListener(accL);
                    delete.setClickable(false);
                }
                else{
                    mSensorManager.registerListener(accL,mAccelermeter,SensorManager.SENSOR_DELAY_NORMAL);
                    delete.setClickable(true);
                }
            }

        });
        //버튼이 변할때만 동작하므로 재실행시 이 함수는 실행되지 않는다.
        //리스너해제를 stop에서 했기에 Start에서 리스너 등록을 하는 부분이다.
        //또한 첫 시작, 재실행시 버튼의 체크상태를 확인해야 가속도센서를 켜고 끌 수 있다.


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(Intent.ACTION_SEND.equals(action)&&type!=null){
            if("text/plain".equals(type)){
                String sharetext = intent.getStringExtra(Intent.EXTRA_TEXT);
                contents.setText(sharetext);
                //add 버튼을 눌러 액티비티를 띄운거랑 인텐트를 받아 액티비티를 띄운것을 구분해야함
                //이후 save를 누를때 startActivity를 사용하도록한다.
            }
            else{ }
        }

        //onCreate에서 받으려고 하였으나 앱 실행중간에 다른 앱에서 인텐트를 보내는 경우 처리해야하기 때문에 start에 넣었다
        //resume 에 넣지 않은 경우는 pause resume이 반복되는 상황에서 if문을 계속 처리하는 건 비효율 적이라고 생각했기에
        //앱이 다시 실행되거나, 처음 실행되거나 할 상황에서 인텐트를 체크한다
        //외부에서 인텐트가 왔을때 처리

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(sw.isChecked()){
            mSensorManager.unregisterListener(accL);
            delete.setClickable(false);

        }
        else{
            mSensorManager.registerListener(accL,mAccelermeter,SensorManager.SENSOR_DELAY_NORMAL);
            delete.setClickable(true);
        }
        // pause에서 리스너를 해제했으므로 resume에서 다시 등록한 것이고
        //onResume에도 if문을 두는 이유 버튼 리스너는 버튼이 바뀌었을때만 동작하기 때문에
        // 재실행, 첫 실행 같은 경우 실행주기상에서 다시 상태를 체크해야한다.
        //버튼이 체크되고 다시 시작하는 상황이 올때 실행주기단계에서 체크를 해야할 곳이 필요하기 때문에


    } //흔들기 모션을 임의적으로 조절하기 위한 switch 를 넣었다.

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(accL);
        // 실행중이 아닐때는 무조건 리스너를 꺼야하기때문에
    }

    @Override
    protected void onStop() {
        super.onStop();
        sf = getSharedPreferences("sFile",MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();
        Boolean swChecked = sw.isChecked();
        editor.putBoolean("switchCheck",swChecked);//스위치 체크상태 저장
        editor.commit();

        if(mSensorManager!=null){
            mSensorManager.unregisterListener(accL);
        }//센서가 pause에서 꺼지지 않았을 경우 제대로 센서를 끄는 if문

        sw.setOnCheckedChangeListener(null);//리스너들을 사용하지 않아 메모리 낭비를 방지한다
        //이 부분을 제거한다면 Listener 등록부분을 onCreate에 한다.
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        mSensorManager.unregisterListener(accL);
    }

}
