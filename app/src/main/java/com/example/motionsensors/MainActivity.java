package com.example.motionsensors;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    static String myResponse = "";
    private Thread thread;
    private SensorManager  sensorManager;
    private Sensor mAccelerometers;
    private Sensor mGyroscope;
    TextView acc, gyr, con, response, status, queue, phoneText;
    TextView accText, gyrText;
    public float[] gravity = new float[3];   //重力在设备x、y、z轴上的分量
    public float[] motion = new float[3];  //过滤掉重力后，加速度在x、y、z上的分量
    private int countA = 0;
    private int countG = 0;
    int TimeA;
    int TimeG;
    int startTimeA;
    int startTimeG;
    int endTimeA;
    int endTimeG;
    int startA = 0;
    int startG = 0;
    float valueX = 0;
    float valueY = 0;
    float valueZ = 0;
    String strAx = "";
    String strAy = "";
    String strAz = "";
    String strGx = "";
    String strGy = "";
    String strGz = "";
    String uploadTime = "";
    String uploadAx = "";
    String uploadAy = "";
    String uploadAz = "";
    String uploadGx = "";
    String uploadGy = "";
    String uploadGz = "";
    int uploadA = 0;
    int uploadG = 0;
    //-------系統時間--------//
    int year;
    int month;
    int day;
    int hour; // 0-23
    int minute;
    int second;
    int msec;
    int presecond=61;

    /*
    2:sony-330
    3:M8-90
    4:mi-90
    */
    int phoneNum = 2;
    int slide2 = -1;
    int slide3 = 0;
    int slide4 = -1;
    int collection = 90;

    String msecstr = "";
    //-------系統時間--------//
    //private
    Boolean startFlag=false;
    Boolean uploadFlag=false;
    Boolean matchPoint=false;
    public static Handler handler = new Handler();
    int speed=100;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private  float[] angle = {0,0,0};

    Queue<String> uploadTime_queue = new LinkedList<>();
    Queue<String> uploadAx_queue = new LinkedList<>();
    Queue<String> uploadAy_queue = new LinkedList<>();
    Queue<String> uploadAz_queue = new LinkedList<>();
    Queue<String> uploadGx_queue = new LinkedList<>();
    Queue<String> uploadGy_queue = new LinkedList<>();
    Queue<String> uploadGz_queue = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //tClock = (TextClock) findViewById(R.id.textClock);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometers = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        acc= findViewById(R.id.acc);
        gyr = findViewById(R.id.gry);
        accText= findViewById(R.id.accText);
        gyrText= findViewById(R.id.gyrText);
        response = findViewById(R.id.respone);
        status = findViewById(R.id.staus);
        queue = findViewById(R.id.queue);
        con= findViewById(R.id.conter);
        phoneText= findViewById(R.id.phoneNum);
        phoneText.setText("Phone: " + phoneNum);
        Button start = findViewById(R.id.start);
        Button pause = findViewById(R.id.pause);
        Button stop = findViewById(R.id.stop);
        RequestQueue ExampleRequestQueue = Volley.newRequestQueue(this);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Start");
                startFlag = true;
                uploadFlag = true;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Close");
                response.setText("waiting...");
                startFlag = false;
                uploadFlag = false;
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Pause");
                startFlag = false;
                uploadFlag = true;
            }
        });

        while(true){
            Calendar cal = Calendar.getInstance();
            msec = cal.get(Calendar.MILLISECOND);
            Log.d("[Test]", ">>>>>>>>>>>>>: " + msec);
            if(msec == 830)
                break;
        }

        //設定定時要執行的方法
        handler.removeCallbacks(updateTimer);
        //設定Delay的時間
        handler.postDelayed(updateTimer, speed);
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,mAccelerometers,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.equals(mAccelerometers) && matchPoint) {
            int TimeNowA = Calendar.getInstance().get(Calendar.MILLISECOND);

            if (countA == 0) {
                strAx = "";
                strAy = "";
                strAz = "";
                startTimeA = Calendar.getInstance().get(Calendar.MILLISECOND);
                //Log.d("[Test]", "\t--startTimeA: " + startTimeA);

                if (Calendar.getInstance().get(Calendar.MILLISECOND) < 10 && Calendar.getInstance().get(Calendar.MILLISECOND) > 0) {
                    startA = 1;

                    Calendar startTime = Calendar.getInstance();

                    if  (phoneNum == 2) {
                        startTime.add(Calendar.SECOND, slide2);
                    } else if  (phoneNum == 3) {
                        startTime.add(Calendar.SECOND, slide3);
                    } else if  (phoneNum == 4) {
                        startTime.add(Calendar.SECOND, slide4);
                    }

                    int uploadYear = startTime.get(Calendar.YEAR);
                    int uploadMonth = startTime.get(Calendar.MONTH)+1;
                    int uploadDay = startTime.get(Calendar.DAY_OF_MONTH);
                    int uploadHour = startTime.get(Calendar.HOUR_OF_DAY);
                    int uploadMinute = startTime.get(Calendar.MINUTE);
                    int uploadSecond = startTime.get(Calendar.SECOND);
                    int uploadmsec = startTime.get(Calendar.MILLISECOND);

                    String upload = uploadYear + "-" + uploadMonth + "-" + uploadDay + " " + uploadHour + ":" + uploadMinute + ":" + uploadSecond;// + "." + uploadmsec;
                    Log.d("[Test]", "[uploadTime]: " + upload);

                    if(startFlag) {
                        uploadTime_queue.offer(upload);
                    }
                }
            }

            if (startA == 1) {
                countA++;
                //Log.d("[Test]", "[TimeNowA]: " + Calendar.getInstance().get(Calendar.MILLISECOND));
                Log.d("[Test]", "\t--countA: " + countA);

                for (int i = 0; i < 3; i++) {
                    gravity[i] = (float) (0.1 * event.values[i] + 0.9 * gravity[i]);
                    motion[i] = event.values[i] - gravity[i];
                }

                valueX = (float) ((Math.round(motion[0] * 1000))) / 1000;
                valueY = (float) ((Math.round(motion[1] * 1000))) / 1000;
                valueZ = (float) ((Math.round(motion[2] * 1000))) / 1000;
                strAx += valueX + ",";
                strAy += valueY + ",";
                strAz += valueZ + ",";

                if (countA == collection) {
                    countA = 0;
                    startA = 0;
                    uploadA = 1;

                    if (startFlag) {
                        uploadAx_queue.offer(strAx);
                        uploadAy_queue.offer(strAy);
                        uploadAz_queue.offer(strAz);
                    }

                    endTimeA = Calendar.getInstance().get(Calendar.MILLISECOND);
                    TimeA = endTimeA - startTimeA;
                    if (TimeA < 0)
                        TimeA += 1000;
                    //Log.d("[Test]", "\t\t--endTimeA: " + endTimeA);
                    //Log.d("[Test]", "\t\t--TimeA: " + TimeA);
                    Log.d("[Test]","Acc - \nX:"+strAx+"\n"+"Y:"+strAy+"\n"+"Z:"+strAz+"\n");
                }
            }
        }

        if (event.sensor.equals(mGyroscope) && matchPoint) {
            /*
            Log.d("[Test]", "<timestamp++>: " + timestamp);
            if (timestamp != 0) {
                //  event.timesamp表示當前的時間，單位是納秒（1百萬分之一毫秒）
                final float dT = (event.timestamp - timestamp) * NS2S;
                Log.d("[Test]", "<dT>: " + dT);
                angle[0] = event.values[0] * dT;
                angle[1] = event.values[1] * dT;
                angle[2] = event.values[2] * dT;

                Log.d("[Test]", "<value>: " + event.values[0] + "\t" + event.values[1] + "\t" + event.values[2]);
                Log.d("[Test]", "<angle>: " + angle[0] + "\t" + angle[1] + "\t" + angle[2]);
            }
            timestamp = event.timestamp;
            Log.d("[Test]", "<timestamp-->: " + timestamp);
            */

            int TimeNowG = Calendar.getInstance().get(Calendar.MILLISECOND);

            if (countG == 0) {
                strGx = "";
                strGy = "";
                strGz = "";
                startTimeG = Calendar.getInstance().get(Calendar.MILLISECOND);
                //Log.d("[Test]", "\t--startTimeG: " + startTimeG);

                if (Calendar.getInstance().get(Calendar.MILLISECOND) < 10 && Calendar.getInstance().get(Calendar.MILLISECOND) > 0)
                    startG = 1;
            }

            if (startG == 1) {
                countG++;
                //Log.d("[Test]", "[TimeNowG]: " + Calendar.getInstance().get(Calendar.MILLISECOND));
                //Log.d("[Test]", "\t--countG: " + countG);

                strGx += event.values[0] + ",";
                strGy += event.values[1] + ",";
                strGz += event.values[2] + ",";

                if (countG == collection) {
                    countG = 0;
                    startG = 0;
                    uploadG = 1;
                    if (startFlag) {
                        uploadGx_queue.offer(strGx);
                        uploadGy_queue.offer(strGy);
                        uploadGz_queue.offer(strGz);
                    }

                    endTimeG = Calendar.getInstance().get(Calendar.MILLISECOND);
                    TimeG = endTimeG - startTimeG;
                    if (TimeG < 0)
                        TimeG += 1000;
                    //Log.d("[Test]", "\t\t--endTimeG: " + endTimeG);
                    //Log.d("[Test]", "\t\t--TimeG: " + TimeG);
                    Log.d("[Test]","Gyr - \nX:"+strGx+"\n"+"Y:"+strGy+"\n"+"Z:"+strGz+"\n");
                }
            }
        }

        //acc.setText("TotalValue:" + String.valueOf(second) + ":" + String.valueOf(total) + "\n");
        //gyr.setText("X:"+ strAx +"\n"+"Y:"+ strAy +"\n"+"Z:"+ strAz +"\n");

        //accText.setText("Ax:" + strAx + "\n" + "Ay:" + strAy + "\n" + "Az:" + strAz + "\n");
        //gyrText.setText("Gx:" + strGx + "\n" + "Gy:" + strGy + "\n" + "Gz:" + strGz + "\n");

        accText.setText("Ax:" + strAx + "\n" + "Ay:" + strAy + "\n" + "Az:" + strAz + "\n");
        gyrText.setText("Gx:" + strGx + "\n" + "Gy:" + strGy + "\n" + "Gz:" + strGz + "\n");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    private Runnable updateTimer = new Runnable() {
        public void run() {
            Calendar cal = Calendar.getInstance();

            if (phoneNum == 2) {
                cal.add(Calendar.SECOND, slide2);
            } else if  (phoneNum == 3) {
                cal.add(Calendar.SECOND, slide3);
            } else if  (phoneNum == 4) {
                cal.add(Calendar.SECOND, slide4);
            }

            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH)+1;
            day = cal.get(Calendar.DAY_OF_MONTH);
            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);
            second = cal.get(Calendar.SECOND);
            msec = cal.get(Calendar.MILLISECOND);

            //Log.d("[Test]", "--------------");
            //Log.d("[Test]", ">>>>>>>>time: " + msec);
            String showTime = year + "年" + month + "月" + day + "日" + hour + ":" + minute + ":" + second + "." + msec;
            con.setText(showTime);
            //Log.d("[Test]", "<showTime>" + showTime);

            queue.setText("Queue size: " + uploadTime_queue.size());

            matchPoint = true;

            if (uploadA == 1  && uploadG == 1) {
                uploadA = 0;
                uploadG = 0;
                if (uploadFlag) {

                    Log.d("[Test]", "<SEND!!!!!>");

                    OkHttpClient client = new OkHttpClient();

                    //String time = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);

                    uploadTime = uploadTime_queue.poll();
                    uploadAx = uploadAx_queue.poll();
                    uploadAy = uploadAy_queue.poll();
                    uploadAz = uploadAz_queue.poll();
                    uploadGx = uploadGx_queue.poll();
                    uploadGy = uploadGy_queue.poll();
                    uploadGz = uploadGz_queue.poll();

                    if (uploadTime != null && uploadAx != null && uploadGx != null) {
                        String url = "http://140.134.26.138/VIPS/updateAcc" + phoneNum + ".php?" +
                                "accx=" + uploadAx + "&accy=" + uploadAy + "&accz="+ uploadAz +
                                "&gyrx=" + uploadGx + "&gyry="+ uploadGy + "&gyrz="+ uploadGz +
                                "&phone=" + phoneNum + "&date=" + uploadTime;

                        Log.d("[Test-upload]", "<Time> " + uploadTime);
                        Log.d("[Test-upload]", "<URL> " + url);

                        //System.out.println(url);
                        Request request = new Request.Builder()
                                .url(url)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("[ERROR]!!!!!","Failure", e);
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                myResponse = response.body().string();
                                if (response.isSuccessful()) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.this.response.setText("upload...");
                                        }
                                    });
                                    Log.d("[Test-upload]", "<Success>");
                                    Log.d("[Test-upload]", "<Response> " + myResponse);
                                } else{
                                    Log.d("[Test-upload]", "<NotSuccess>");
                                    Log.e("[ERROR]!!!!!","NotSuccess");
                                    Intent intent = new Intent();
                                    intent.setClass(MainActivity.this, TestActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    }

                }
            }
            handler.postDelayed(this, speed);
        }
    };
}
