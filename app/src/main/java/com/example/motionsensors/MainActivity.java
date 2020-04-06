package com.example.motionsensors;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.SyncFailedException;
import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Thread thread;
    private SensorManager  sensorManager;
    private Sensor mAccelerometers;
    private Sensor mGyroscope;
    TextView acc,gry,con,re;
    Timer timer;
    public float[] gravity = new float[3];   //重力在设备x、y、z轴上的分量
    public float[] motion = new float[3];  //过滤掉重力后，加速度在x、y、z上的分量
    private int counter = 0;
    int tt=0;
    int speed=100;
    int startTime=0;
    float total=0;
    String Xstr ="";
    String Ystr ="";
    String Zstr ="";
    float Xvalue=0;
    float Yvalue=0;
    float Zvalue=0;
    String uploadvalueX="";
    String uploadvalueY="";
    String uploadvalueZ="";
    //-------系統時間--------//
    private TextClock tClock;
    int year ;
    int month ;
    int day ;
    int hour ; // 0-23
    int minute ;
    int second ;
    int pres=100;
    private Long sTime;
    //-------系統時間--------//
    Boolean flag=false;
    Boolean matchpoint=false;
    public static Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tClock = (TextClock) findViewById(R.id.textClock);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometers = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        acc=(TextView)findViewById(R.id.acc);
        gry=(TextView)findViewById(R.id.gry);
        re=(TextView)findViewById(R.id.respone);
        con=(TextView)findViewById(R.id.conter);
        Button btn =(Button) findViewById(R.id.button);
        RequestQueue ExampleRequestQueue = Volley.newRequestQueue(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(flag)) {
                    re.setText("Start");
                    flag = true;
                    //thread=new Thread(httpGET);
                    //thread.start();

                }
                else {
                    re.setText("Close");
                    flag = false;
                }
            }
        });
        Calendar c = Calendar.getInstance();
        pres=c.get(Calendar.SECOND);
        sTime = System.currentTimeMillis();
        //設定定時要執行的方法
        handler.removeCallbacks(updateTimer);
        //設定Delay的時間
        handler.postDelayed(updateTimer, 100);
        //Calendar c = Calendar.getInstance();
        //second=c.get(Calendar.SECOND);
        //pres= second;
/*            if(matchpoint) {


                timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Calendar c = Calendar.getInstance();
                                year = c.get(Calendar.YEAR);
                                month = c.get(Calendar.MONTH);
                                day = c.get(Calendar.DAY_OF_MONTH);
                                hour = c.get(Calendar.HOUR_OF_DAY);
                                minute = c.get(Calendar.MINUTE);
                                second = c.get(Calendar.SECOND);

                                con.setText("時間" + year + "年" + month + "月" + day + "日" + hour + ":" + minute + ":" + second);

                                tt += 1;


                                //con.setText(String.valueOf(tt)+"秒");

                                //con.setText(String.valueOf(tt)+"秒"+String.valueOf(counter+1)+"次");
                                if (tt == 10) {
                                    year = c.get(Calendar.YEAR);
                                    month = c.get(Calendar.MONTH);
                                    day = c.get(Calendar.DAY_OF_MONTH);
                                    hour = c.get(Calendar.HOUR_OF_DAY);
                                    minute = c.get(Calendar.MINUTE);
                                    second = c.get(Calendar.SECOND);
                                    con.setText("時間" + year + "年" + month + "月" + day + "日" + hour + ":" + minute + ":" + second);
                                    if (flag) {
                                        OkHttpClient client = new OkHttpClient();

                                        //String url = "https://reqres.in/api/users?page=2";

                                        String d = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
                                        String url = "http://140.134.26.135/VIPS/accupdate.php?acc=" + uploadvalue + "&date=" + d;
                                        System.out.println(url);
                                        Request request = new Request.Builder()
                                                .url(url)
                                                .build();

                                        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                e.printStackTrace();
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    final String myResponse = response.body().string();

                                                    MainActivity.this.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            re.setText("upload...");
                                                        }
                                                    });
                                                }
                                            }
                                        });

                                    }


                                }

                            }
                        });

                    }
                };

                timer.schedule(task, 100, 100);
            }*/
        }


    @Override
    protected void onResume() {

        super.onResume();
        sensorManager.registerListener(this,mAccelerometers,SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {

        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

            Calendar c = Calendar.getInstance();



        //String value = "X-axis" + String.valueOf(sensorEvent.values[0]) + "\n" + "Y-axis" + String.valueOf(sensorEvent.values[1]) + "\n" + "Z-axis" + String.valueOf(sensorEvent.values[2]) + "\n";
            if (sensorEvent.sensor.equals(mAccelerometers)&& matchpoint) {

                if(tt-startTime>=1)
                {
                    if(counter==0) {


                        Xstr="";
                        Ystr="";
                        Zstr="";

                    }
                    startTime=tt;
                    for (int i = 0; i < 3; i++) {
                        gravity[i] = (float) (0.1 * sensorEvent.values[i] + 0.9 * gravity[i]);
                        motion[i] = sensorEvent.values[i] - gravity[i];
                    }
                    counter++;

                    //value = "X-axis"+String.valueOf(motion[0])+"\n"+"Y-axis"+String.valueOf(motion[1])+"\n"+"Z-axis"+String.valueOf(motion[2])+"\n";
                    total = (float)(Math.round((float) (Math.pow((Math.pow(motion[0], 2) + Math.pow(motion[1], 2) + Math.pow(motion[2], 2)), 0.5))*10000))/10000;

                    //value=value+String.valueOf(total)+",";
                    Xvalue=(float)((Math.round(motion[0]*1000)))/1000;
                    Yvalue=(float)((Math.round(motion[1]*1000)))/1000;
                    Zvalue=(float)((Math.round(motion[2]*1000)))/1000;
                    Xstr+=String.valueOf(Xvalue)+",";
                    Ystr+=String.valueOf(Yvalue)+",";
                    Zstr+=String.valueOf(Zvalue)+",";


                }
                if(counter==10){
                    startTime=0;
                    counter=0;
                    tt=0;
                    uploadvalueX=Xstr;
                    uploadvalueY=Ystr;
                    uploadvalueZ=Zstr;

                }

            }

        acc.setText("TotalValue:"+String.valueOf(second)+":"+String.valueOf(total)+"\n");
        gry.setText("X:"+Xstr+"\n"+"Y:"+Ystr+"\n"+"Z:"+Zstr+"\n");

            /*if (sensorEvent.sensor.equals(mGyroscope)) {

            }*/
        /*if(counter%50 == 0) {
            acc.setText("TotalValue:"+String.valueOf(total)+"\n");
            gry.setText(value);
            counter=1;
        }
        counter++;*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    private Runnable updateTimer = new Runnable() {
        public void run() {

            Calendar c = Calendar.getInstance();

            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH)+1;
            day = c.get(Calendar.DAY_OF_MONTH);
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            second = c.get(Calendar.SECOND);

 ;
            if(second!=pres){
                tt += 1;
                con.setText("時間" + year + "年" + month + "月" + day + "日" + hour + ":" + minute + ":" + second);
                matchpoint = true;
                pres=-1;
            }



            //con.setText(String.valueOf(tt)+"秒"+String.valueOf(counter+1)+"次");

            //con.setText(String.valueOf(tt)+"秒");


            if (tt == 10) {


                //con.setText("時間" + year + "年" + month + "月" + day + "日" + hour + ":" + minute + ":" + second);
                if (flag) {
                    OkHttpClient client = new OkHttpClient();

                    //String url = "https://reqres.in/api/users?page=2";

                    String d = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
                    String url = "http://140.134.26.135/VIPS/accupdate.php?xacc=" + uploadvalueX + "&yacc="+uploadvalueY+"&zacc="+uploadvalueZ+"&date=" + d;
                    System.out.println(url);
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                final String myResponse = response.body().string();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        re.setText("upload...");
                                    }
                                });
                            }
                        }
                    });

                }


            }



            handler.postDelayed(this, speed);
        }
    };
}
