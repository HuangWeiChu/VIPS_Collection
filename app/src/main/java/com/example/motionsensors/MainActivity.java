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

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Queue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    TextView acc, gyr;
    TextView accText, gyrText;
    TextView response, status, queue, time, phone;

    // 計算參數
    public float[] gravity = new float[3]; // 重力在x、y、z軸上的分量
    public float[] motion = new float[3]; // 過濾掉重力後，加速度在x、y、z上的分量

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private float[] angle = {0, 0, 0};

    // 紀錄回應參數
    static String myResponse = "";

    // 記數參數
    private int countA = 0;
    private int countG = 0;

    // 時間參數
    int TimeA;
    int TimeG;
    int startTimeA;
    int startTimeG;
    int endTimeA;
    int endTimeG;

    // 其他時間參數
    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    long offset = TestActivity.offset;

    // 權限參數
    int startA = 0;
    int startG = 0;

    // 數值參數
    float valueX = 0;
    float valueY = 0;
    float valueZ = 0;

    // 數據參數
    String strAx = "";
    String strAy = "";
    String strAz = "";
    String strGx = "";
    String strGy = "";
    String strGz = "";

    // 上傳參數
    String uploadTime = "";
    String uploadAx = "";
    String uploadAy = "";
    String uploadAz = "";
    String uploadGx = "";
    String uploadGy = "";
    String uploadGz = "";

    // 上傳權限參數
    int uploadA = 0;
    int uploadG = 0;

    //-------限制參數--------//
    /*
    2: sony-330 (2s/2s)
    3: M8-90 (9s/9s)
    4: mi-90-180 (9s/4s)
    */
    int phoneNum = TestActivity.phoneNum;

    int collectionAcc = 90;
    int collectionGyr = 90;

    int timeLimit = 50;

    //-------啟動權限--------//
    Boolean startFlag = false;
    Boolean uploadFlag = false;
    Boolean errorFlag = false;
    Boolean matchPoint = false;

    public static Handler handler = new Handler();
    int speed = 100;

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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        acc = findViewById(R.id.acc);
        gyr = findViewById(R.id.gry);
        accText = findViewById(R.id.accText);
        gyrText = findViewById(R.id.gyrText);

        response = findViewById(R.id.respone);
        status = findViewById(R.id.staus);
        queue = findViewById(R.id.queue);
        time = findViewById(R.id.conter);

        phone = findViewById(R.id.phoneNum);
        phone.setText("Phone: " + phoneNum);

        Button start = findViewById(R.id.start);
        Button pause = findViewById(R.id.pause);
        Button stop = findViewById(R.id.stop);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Start");
                response.setText("preparing...");
                startFlag = true;
                uploadFlag = true;
                errorFlag = false;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Close");
                response.setText("waiting...");
                startFlag = false;
                uploadFlag = false;
                errorFlag = false;
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Pause");
                startFlag = false;
                uploadFlag = true;
                errorFlag = false;
            }
        });

        //設定定時要執行的方法
        handler.removeCallbacks(updateTimer);
        //設定Delay的時間
        handler.postDelayed(updateTimer, speed);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // 調整sensor取樣頻率
        if (phoneNum == 2) {
            collectionAcc = 330;
            collectionGyr = 330;
        } else if (phoneNum == 3) {
            collectionAcc = 90;
            collectionGyr = 90;
        } else if (phoneNum == 4) {
            collectionAcc = 90;
            collectionGyr = 180;
        }

        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(mAccelerometer) && matchPoint) {
            //Log.d("[Time_Acc]", "\tEveryTimeA: " + (System.currentTimeMillis() + offset) % 1000);
            if (countA == 0) {
                strAx = "";
                strAy = "";
                strAz = "";
                startTimeA = (int) (System.currentTimeMillis() + offset);
                //Log.d("[Time_Acc]", "\tstartTimeA: " + startTimeA);

                // 取得毫秒時間
                long msec = (System.currentTimeMillis() + offset) % 1000;
                if (msec <= timeLimit && msec > 0) {
                    startA = 1;
                    // 取得時間
                    long startTime = System.currentTimeMillis();

                    // 校正時間點對齊
                    startTime += offset;

                    // 取得時間數據
                    String uploadTime = dfm.format(new Timestamp(startTime));

                    Log.d("[Time_upload]", "[uploadTime]: " + uploadTime);

                    if (startFlag) {
                        uploadTime_queue.offer(uploadTime);
                    }
                }
            }

            if (startA == 1) {
                countA++;
                //Log.d("[Time_Acc]", "[TimeNowA]: " + (System.currentTimeMillis() + offset) % 1000);
                //Log.d("[Count_Acc]", "\tcountA: " + countA);

                // 移除重力
                for (int i = 0; i < 3; i++) {
                    gravity[i] = (float) (0.1 * event.values[i] + 0.9 * gravity[i]);
                    motion[i] = event.values[i] - gravity[i];
                }

                valueX = (float) Math.round(motion[0] * 1000) / 1000;
                valueY = (float) Math.round(motion[1] * 1000) / 1000;
                valueZ = (float) Math.round(motion[2] * 1000) / 1000;
                strAx += valueX + ",";
                strAy += valueY + ",";
                strAz += valueZ + ",";

                if (countA == collectionAcc) {
                    countA = 0;
                    startA = 0;
                    uploadA = 1;

                    if (startFlag) {
                        uploadAx_queue.offer(strAx);
                        uploadAy_queue.offer(strAy);
                        uploadAz_queue.offer(strAz);
                    }

                    endTimeA = (int) (System.currentTimeMillis() + offset);
                    TimeA = endTimeA - startTimeA;
                    //Log.d("[Time_Acc]", "\t\tendTimeA: " + endTimeA);
                    Log.d("[Time_Acc]", "\t\tTimeA: " + TimeA);
                    //Log.d("[Value_Acc]", "Acc - \nX: " + strAx + "\n" + "Y: " + strAy + "\n" + "Z: " + strAz + "\n");
                }
            }
        }

        if (event.sensor.equals(mGyroscope) && matchPoint) {
            //Log.d("[Time_Gyr]", "\tEveryTimeG: " + (System.currentTimeMillis() + offset) % 1000);

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
            Log.d("[Test]", "<timestamp>: " + timestamp);
            */

            if (countG == 0) {
                strGx = "";
                strGy = "";
                strGz = "";
                startTimeG = (int) (System.currentTimeMillis() + offset);
                //Log.d("[Time_Gyr]", "\tstartTimeG: " + startTimeG);

                // 取得毫秒時間
                long msec = (System.currentTimeMillis() + offset) % 1000;
                if (msec <= timeLimit && msec > 0)
                    startG = 1;
            }

            if (startG == 1) {
                countG++;
                //Log.d("[Time_Gyr]", "[TimeNowG]: " + (System.currentTimeMillis() + offset) % 1000);
                //Log.d("[Count_Gyr]", "\tcountG: " + countG);

                strGx += event.values[0] + ",";
                strGy += event.values[1] + ",";
                strGz += event.values[2] + ",";

                if (countG == collectionGyr) {
                    countG = 0;
                    startG = 0;
                    uploadG = 1;
                    if (startFlag) {
                        uploadGx_queue.offer(strGx);
                        uploadGy_queue.offer(strGy);
                        uploadGz_queue.offer(strGz);
                    }

                    endTimeG = (int) (System.currentTimeMillis() + offset);
                    TimeG = endTimeG - startTimeG;
                    //Log.d("[Time_Gyr]", "\t\tendTimeG: " + endTimeG);
                    Log.d("[Time_Gyr]", "\t\tTimeG: " + TimeG);
                    //Log.d("[Value_Gyr]", "Gyr - \nX: " + strGx + "\n" + "Y: " + strGy + "\n" + "Z: " + strGz + "\n");
                }
            }
        }

        acc.setText("Acc count: " + countA);
        gyr.setText("Gyr count: " + countG);

        accText.setText("Ax:" + strAx + "\n" + "Ay:" + strAy + "\n" + "Az:" + strAz + "\n");
        gyrText.setText("Gx:" + strGx + "\n" + "Gy:" + strGy + "\n" + "Gz:" + strGz + "\n");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private Runnable updateTimer = new Runnable() {
        public void run() {
            // 取得時間
            long getTime = System.currentTimeMillis();

            // 校正時間點對齊
            getTime += offset;

            // 取得時間數據
            String showTime = dfm.format(new Timestamp(getTime));

            time.setText(showTime);
            //Log.d("[Time_show]", "<showTime>" + showTime);

            queue.setText("Queue size: " + uploadTime_queue.size());

            matchPoint = true;

            if (uploadA == 1 && uploadG == 1) {
                uploadA = 0;
                uploadG = 0;
                if (uploadFlag) {
                    //Log.d("[Upload]", "<SEND!!!!!>");

                    OkHttpClient client = new OkHttpClient();

                    // 取出佇列數據
                    uploadTime = uploadTime_queue.poll();
                    uploadAx = uploadAx_queue.poll();
                    uploadAy = uploadAy_queue.poll();
                    uploadAz = uploadAz_queue.poll();
                    uploadGx = uploadGx_queue.poll();
                    uploadGy = uploadGy_queue.poll();
                    uploadGz = uploadGz_queue.poll();

                    if (uploadTime != null && uploadAx != null && uploadGx != null) {
                        String url = "http://140.134.26.138/VIPS/updateAcc" + phoneNum + ".php?" +
                                "accx=" + uploadAx + "&accy=" + uploadAy + "&accz=" + uploadAz +
                                "&gyrx=" + uploadGx + "&gyry=" + uploadGy + "&gyrz=" + uploadGz +
                                "&phone=" + phoneNum + "&date=" + uploadTime;

                        //Log.d("[Upload]", "<Time> " + uploadTime);
                        //Log.d("[Upload]", "<URL> " + url);

                        Request request = new Request.Builder()
                                .url(url)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                errorFlag = true;
                                myResponse = "<html><i>" + e.getMessage() + "</i></html>";
                                Log.d("[Upload]", "<Error> " + e.getMessage());
                                Log.e("[ERROR]!!!!!", "Failure", e);
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                myResponse = response.body().string();
                                if (response.isSuccessful()) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.this.response.setText("uploading...");
                                        }
                                    });
                                    Log.d("[Upload]", "<Success> " + myResponse);
                                } else {
                                    errorFlag = true;
                                    Log.d("[Upload]", "<NotSuccess>");
                                    Log.e("[ERROR]!!!!!", "NotSuccess");
                                }
                            }
                        });

                        // 當傳送錯誤時轉跳頁面
                        if (errorFlag) {
                            errorFlag = false;
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, TestActivity.class);
                            startActivity(intent);
                        }
                    }

                }
            }
            handler.postDelayed(this, speed);
        }
    };
}
