package com.example.motionsensors;

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

public class MainActivity_original extends AppCompatActivity implements SensorEventListener {
    private Thread thread;
    private SensorManager  sensorManager;
    private Sensor mAccelerometers;
    private Sensor mGyroscope;
    private Sensor mLinearAccelerometers;
    TextView acc, gyr, con, response, staus, queue, phoneText;
    TextView accText, gyrText;
    float total=0;
    public float[] gravity = new float[3];   //重力在设备x、y、z轴上的分量
    public float[] motion = new float[3];  //过滤掉重力后，加速度在x、y、z上的分量
    private int countA = 0;
    private int countG = 0;
    private int countL = 0;
    int TimeA;
    int TimeG;
    int TimeL;
    int startTimeA;
    int startTimeG;
    int startTimeL;
    int endTimeA;
    int endTimeG;
    int endTimeL;
    int startA = 0;
    int startG = 0;
    int startL = 0;
    float Xvalue = 0;
    float Yvalue = 0;
    float Zvalue = 0;
    float XLvalue = 0;
    float YLvalue = 0;
    float ZLvalue = 0;
    float XOvalue = 0;
    float YOvalue = 0;
    float ZOvalue = 0;
    String XAstr = "";
    String YAstr = "";
    String ZAstr = "";
    String XGstr = "";
    String YGstr = "";
    String ZGstr = "";
    String XLstr = "";
    String YLstr = "";
    String ZLstr = "";
    String XOstr = "";
    String YOstr = "";
    String ZOstr = "";
    String uploadTime = "";
    String uploadAx = "";
    String uploadAy = "";
    String uploadAz = "";
    String uploadGx = "";
    String uploadGy = "";
    String uploadGz = "";
    String uploadLx = "";
    String uploadLy = "";
    String uploadLz = "";
    String uploadOx = "";
    String uploadOy = "";
    String uploadOz = "";
    int uploadA = 0;
    int uploadG = 0;
    int uploadL = 0;
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
    2:sony
    3:M8
    4:mi
    */
    int phonenum = 3;
    int phoneNum = phonenum;
    int slide2 = 0;
    int slide3 = 0;
    int slide4 = 0;

    String msecstr = "";
    //-------系統時間--------//
    //private
    Boolean startflag=false;
    Boolean uploadflag=false;
    Boolean matchpoint=false;
    public static Handler handler = new Handler();
    int speed=100;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private  float[] angle = {0,0,0};

    Queue<String> uploadTime_queue = new LinkedList<String>();
    Queue<String> uploadAx_queue = new LinkedList<String>();
    Queue<String> uploadAy_queue = new LinkedList<String>();
    Queue<String> uploadAz_queue = new LinkedList<String>();
    Queue<String> uploadGx_queue = new LinkedList<String>();
    Queue<String> uploadGy_queue = new LinkedList<String>();
    Queue<String> uploadGz_queue = new LinkedList<String>();
    Queue<String> uploadLx_queue = new LinkedList<String>();
    Queue<String> uploadLy_queue = new LinkedList<String>();
    Queue<String> uploadLz_queue = new LinkedList<String>();
    Queue<String> uploadOx_queue = new LinkedList<String>();
    Queue<String> uploadOy_queue = new LinkedList<String>();
    Queue<String> uploadOz_queue = new LinkedList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //tClock = (TextClock) findViewById(R.id.textClock);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometers = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelerometers = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        acc=(TextView)findViewById(R.id.acc);
        gyr =(TextView)findViewById(R.id.gry);
        accText=(TextView)findViewById(R.id.accText);
        gyrText=(TextView)findViewById(R.id.gyrText);
        response =(TextView)findViewById(R.id.respone);
        staus =(TextView)findViewById(R.id.staus);
        queue =(TextView)findViewById(R.id.queue);
        con=(TextView)findViewById(R.id.conter);
        phoneText=(TextView)findViewById(R.id.phoneNum);
        phoneText.setText("Phone: " + phonenum);
        Button start =(Button) findViewById(R.id.start);
        Button pause =(Button) findViewById(R.id.pause);
        Button stop =(Button) findViewById(R.id.stop);
        RequestQueue ExampleRequestQueue = Volley.newRequestQueue(this);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                staus.setText("Start");
                startflag = true;
                uploadflag = true;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                staus.setText("Close");
                response.setText("waiting...");
                startflag = false;
                uploadflag = false;
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                staus.setText("Pause");
                startflag = false;
                uploadflag = true;
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
        //sensorManager.registerListener(this,mAccelerometers,SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);

        //ensorManager.registerListener(this,mLinearAccelerometers,SensorManager.SENSOR_DELAY_UI);
        //sensorManager.registerListener(this,mAccelerometers,SensorManager.SENSOR_DELAY_UI);
        //sensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_UI);

        //sensorManager.registerListener(this,mLinearAccelerometers,SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this,mAccelerometers,SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(this,mLinearAccelerometers,SensorManager.SENSOR_DELAY_FASTEST);
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

        if (event.sensor.equals(mLinearAccelerometers) && matchpoint) {
            if (countL == 0) {
                XLstr = "";
                YLstr = "";
                ZLstr = "";
                startTimeL = Calendar.getInstance().get(Calendar.MILLISECOND);
                //Log.d("[Test]", "\t--startTimeG: " + startTimeG);

                if (Calendar.getInstance().get(Calendar.MILLISECOND) < 100)
                    startL = 1;
            }

            if (startL == 1) {

                countL++;
                //Log.d("[Test]", "[TimeNowG]: " + Calendar.getInstance().get(Calendar.MILLISECOND));
                //Log.d("[Test]", "\t--countG: " + countG);

                Log.d("[Testg]", "Acc[Linear  ] - " +
                        "X: " + event.values[0] + ", Y: " + event.values[1] + ", Z: " + event.values[2] + "\n");

                XLvalue = (float) ((Math.round(event.values[0] * 1000))) / 1000;
                YLvalue = (float) ((Math.round(event.values[1] * 1000))) / 1000;
                ZLvalue = (float) ((Math.round(event.values[2] * 1000))) / 1000;
                XLstr += String.valueOf(XLvalue) + ",";
                YLstr += String.valueOf(YLvalue) + ",";
                ZLstr += String.valueOf(ZLvalue) + ",";

                if (countL == 90) {
                    countL = 0;
                    startL = 0;
                    uploadL = 1;
                    if (startflag) {
                        //uploadLx_queue.offer(XLstr);
                        //uploadLy_queue.offer(YLstr);
                        //uploadLz_queue.offer(ZLstr);
                    }

                    endTimeL = Calendar.getInstance().get(Calendar.MILLISECOND);
                    TimeL = endTimeL - startTimeL;
                    if (TimeL < 0)
                        TimeL += 1000;
                    //Log.d("[Test]", "\t\t--endTimeG: " + endTimeG);
                    //Log.d("[Test]", "\t\t--TimeG: " + TimeG);
                    Log.d("[Test]","AccL - \nX:"+XLstr+"\n"+"Y:"+YLstr+"\n"+"Z:"+ZLstr+"\n");
                }
            }
        }

        if (event.sensor.equals(mAccelerometers) && matchpoint) {
            int TimeNowA = Calendar.getInstance().get(Calendar.MILLISECOND);

            if (countA == 0) {
                XAstr = "";
                YAstr = "";
                ZAstr = "";
                XOstr = "";
                YOstr = "";
                ZOstr = "";
                startTimeA = Calendar.getInstance().get(Calendar.MILLISECOND);
                //Log.d("[Test]", "\t--startTimeA: " + startTimeA);

                if (Calendar.getInstance().get(Calendar.MILLISECOND) < 10 && Calendar.getInstance().get(Calendar.MILLISECOND) > 0) {
                    startA = 1;

                    Calendar startTime = Calendar.getInstance();

                    if  (phonenum == 2) {
                        startTime.add(Calendar.SECOND, slide2);
                    } else if  (phonenum == 3) {
                        startTime.add(Calendar.SECOND, slide3);
                    } else if  (phonenum == 4) {
                        startTime.add(Calendar.SECOND, slide4);
                    }

                    int uploadyear = startTime.get(Calendar.YEAR);
                    int uploadmonth = startTime.get(Calendar.MONTH)+1;
                    int uploadday = startTime.get(Calendar.DAY_OF_MONTH);
                    int uploadhour = startTime.get(Calendar.HOUR_OF_DAY);
                    int uploadminute = startTime.get(Calendar.MINUTE);
                    int uploadsecond = startTime.get(Calendar.SECOND);
                    int uploadmsec = startTime.get(Calendar.MILLISECOND);

                    String upload = String.valueOf(uploadyear) + "-" + String.valueOf(uploadmonth) + "-" + String.valueOf(uploadday) + " " + String.valueOf(uploadhour) + ":" + String.valueOf(uploadminute) + ":" + String.valueOf(uploadsecond);
                    //+ "." + String.valueOf(uploadmsec);
                    Log.d("[Test]", "[uploadTime]: " + upload);

                    if(startflag) {
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
                Log.d("[Testg]","Acc[Original] - " +
                        "X: " + event.values[0] + ", Y: " + event.values[1] + ", Z: " + event.values[2] + "\n");
                Log.d("[Testg]","Acc[Diff    ] - " +
                        "X: " + motion[0] + ", Y: " + motion[1] + ", Z: " + motion[2] + "\n");

                total = (float) (Math.round((float) (Math.pow((Math.pow(motion[0], 2) + Math.pow(motion[1], 2) + Math.pow(motion[2], 2)), 0.5)) * 10000)) / 10000;

                Xvalue = (float) ((Math.round(motion[0] * 1000))) / 1000;
                Yvalue = (float) ((Math.round(motion[1] * 1000))) / 1000;
                Zvalue = (float) ((Math.round(motion[2] * 1000))) / 1000;
                XOvalue = (float) ((Math.round(event.values[0] * 1000))) / 1000;
                YOvalue = (float) ((Math.round(event.values[1] * 1000))) / 1000;
                ZOvalue = (float) ((Math.round(event.values[2] * 1000))) / 1000;
                XAstr += String.valueOf(Xvalue) + ",";
                YAstr += String.valueOf(Yvalue) + ",";
                ZAstr += String.valueOf(Zvalue) + ",";
                XOstr += String.valueOf(XOvalue) + ",";
                YOstr += String.valueOf(YOvalue) + ",";
                ZOstr += String.valueOf(ZOvalue) + ",";

                if (countA == 90) {
                    countA = 0;
                    startA = 0;
                    uploadA = 1;

                    if (startflag) {
                        uploadAx_queue.offer(XAstr);
                        uploadAy_queue.offer(YAstr);
                        uploadAz_queue.offer(ZAstr);
                        //uploadOx_queue.offer(XOstr);
                        //uploadOy_queue.offer(YOstr);
                        //uploadOz_queue.offer(ZOstr);
                    }

                    endTimeA = Calendar.getInstance().get(Calendar.MILLISECOND);
                    TimeA = endTimeA - startTimeA;
                    if (TimeA < 0)
                        TimeA += 1000;
                    //Log.d("[Test]", "\t\t--endTimeA: " + endTimeA);
                    //Log.d("[Test]", "\t\t--TimeA: " + TimeA);
                    Log.d("[Test]","Acc - \nX:"+XAstr+"\n"+"Y:"+YAstr+"\n"+"Z:"+ZAstr+"\n");
                }
            }
        }

        /*
        if (event.sensor.equals(mAccelerometers) && matchpoint) {
            XAstr=String.valueOf(event.values[0])+",";
            YAstr=String.valueOf(event.values[1])+",";
            ZAstr=String.valueOf(event.values[2])+",";
        }*/

        if (event.sensor.equals(mGyroscope) && matchpoint) {
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
                XGstr = "";
                YGstr = "";
                ZGstr = "";
                startTimeG = Calendar.getInstance().get(Calendar.MILLISECOND);
                //Log.d("[Test]", "\t--startTimeG: " + startTimeG);

                if (Calendar.getInstance().get(Calendar.MILLISECOND) < 100)
                    startG = 1;
            }

            if (startG == 1) {
                countG++;
                //Log.d("[Test]", "[TimeNowG]: " + Calendar.getInstance().get(Calendar.MILLISECOND));
                //Log.d("[Test]", "\t--countG: " + countG);

                XGstr += String.valueOf(event.values[0]) + ",";
                YGstr += String.valueOf(event.values[1]) + ",";
                ZGstr += String.valueOf(event.values[2]) + ",";

                if (countG == 90) {
                    countG = 0;
                    startG = 0;
                    uploadG = 1;
                    if (startflag) {
                        uploadGx_queue.offer(XGstr);
                        uploadGy_queue.offer(YGstr);
                        uploadGz_queue.offer(ZGstr);
                    }

                    endTimeG = Calendar.getInstance().get(Calendar.MILLISECOND);
                    TimeG = endTimeG - startTimeG;
                    if (TimeG < 0)
                        TimeG += 1000;
                    //Log.d("[Test]", "\t\t--endTimeG: " + endTimeG);
                    //Log.d("[Test]", "\t\t--TimeG: " + TimeG);
                    Log.d("[Test]","Gyr - \nX:"+XGstr+"\n"+"Y:"+YGstr+"\n"+"Z:"+ZGstr+"\n");
                }
            }
        }

        //acc.setText("TotalValue:" + String.valueOf(second) + ":" + String.valueOf(total) + "\n");
        //gyr.setText("X:"+ XAstr +"\n"+"Y:"+ YAstr +"\n"+"Z:"+ ZAstr +"\n");

        //accText.setText("Ax:" + XAstr + "\n" + "Ay:" + YAstr + "\n" + "Az:" + ZAstr + "\n");
        //gyrText.setText("Gx:" + XGstr + "\n" + "Gy:" + YGstr + "\n" + "Gz:" + ZGstr + "\n");

        accText.setText("Ax:" + XAstr + "\n" + "Ay:" + YAstr + "\n" + "Az:" + ZAstr + "\n");
        gyrText.setText("Gx:" + XGstr + "\n" + "Gy:" + YGstr + "\n" + "Gz:" + ZGstr + "\n");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    private Runnable updateTimer = new Runnable() {
        public void run() {
            Calendar cal = Calendar.getInstance();

            if (phonenum == 2) {
                cal.add(Calendar.SECOND, slide2);
            } else if  (phonenum == 3) {
                cal.add(Calendar.SECOND, slide3);
            } else if  (phonenum == 4) {
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

            matchpoint = true;

            if (uploadA == 1  && uploadG == 1 && uploadL == 1) {
                uploadA = 0;
                uploadG = 0;
                uploadL = 0;
                if (uploadflag) {

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

                    uploadLx = uploadLx_queue.poll();
                    uploadLy = uploadLy_queue.poll();
                    uploadLz = uploadLz_queue.poll();

                    uploadOx = uploadOx_queue.poll();
                    uploadOy = uploadOy_queue.poll();
                    uploadOz = uploadOz_queue.poll();

                    if (uploadTime != null) {
                        String url = "http://140.134.26.138/VIPS/accupdate" + phoneNum + ".php?" +
                                "xacc=" + uploadAx + "&yacc=" + uploadAy + "&zacc="+ uploadAz +
                                "&xgyr=" + uploadGx + "&ygyr="+ uploadGy + "&zgyr="+ uploadGz +
                                "&xlcc=" + uploadLx + "&ylcc=" + uploadLy + "&zlcc="+ uploadLz +
                                "&xocc=" + uploadOx + "&yocc=" + uploadOy + "&zocc="+ uploadOz +
                                "&pnum=" + phoneNum + "&date=" + uploadTime;

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
                                final String myResponse = response.body().string();
                                Log.d("[Test-upload]", "<Response> " + myResponse);
                                if (response.isSuccessful()) {
                                    MainActivity_original.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity_original.this.response.setText("upload...");
                                        }
                                    });
                                    Log.d("[Test-upload]", "<Success>");
                                } else{
                                    Log.d("[Test-upload]", "<NotSuccess>");
                                    Log.e("[ERROR]!!!!!","NotSuccess");
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
