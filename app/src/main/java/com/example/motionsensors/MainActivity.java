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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

    boolean showCount = false;
    boolean showTime = false;

    private SensorManager sensorManager;
    private Sensor mSensor;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    TextView acc, gyr;
    TextView accText, gyrText, rotText;
    TextView response, status, queue, upload, error, time, phone;

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
    private int countR = 0;

    // 時間參數
    int TimeA;
    int TimeG;
    int TimeR;
    int startTimeA;
    int startTimeG;
    int startTimeR;
    int endTimeA;
    int endTimeG;
    int endTimeR;
    String preTimeA = "";
    String preTimeG = "";
    String preTimeR = "";
    String timerA = "";
    String timerG = "";
    String timerR = "";

    // 其他時間參數
    //DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    long offset = TestActivity.offset;

    // 數值參數
    float valueAx = 0;
    float valueAy = 0;
    float valueAz = 0;
    float valueGx = 0;
    float valueGy = 0;
    float valueGz = 0;
    float valueRx = 0;
    float valueRy = 0;
    float valueRz = 0;

    // 數據參數
    String strAx = "";
    String strAy = "";
    String strAz = "";
    String strGx = "";
    String strGy = "";
    String strGz = "";
    String strRx = "";
    String strRy = "";
    String strRz = "";

    // 上傳參數
    Integer uploadIndex = 0;
    String uploadText = "";
    String uploadTime = "";
    String uploadTimeG = "";
    String uploadTimeR = "";
    String uploadAx = "";
    String uploadAy = "";
    String uploadAz = "";
    String uploadGx = "";
    String uploadGy = "";
    String uploadGz = "";
    String uploadRx = "";
    String uploadRy = "";
    String uploadRz = "";


    // 上傳權限參數
    int uploadA = 0;
    int uploadG = 0;
    int uploadR = 0;

    //-------限制參數--------//
    /*
    2: sony-330 (2s/2s)
    3: M8-90 (9s/9s)
    4: mi-90-180 (9s/4s)
    5: pink-
    */
    int phoneNum = TestActivity.phoneNum;

    int collectionAcc = 90;
    int collectionGyr = 90;
    int saveSize = 2000;
    int savedSize = 0;

    //-------啟動權限--------//
    Boolean startFlag = false;
    Boolean uploadFlag = false;
    Boolean errorFlag = false;
    Boolean saveFlag = false;
    Boolean sendFlag = false;
    Boolean stopFlag = false;
    Boolean matchPoint = false;

    public static Handler handler = new Handler();
    int speed = 100;

    LinkedList<String> uploadTime_queue = new LinkedList<>();
    LinkedList<String> uploadTimeG_queue = new LinkedList<>();
    LinkedList<String> uploadTimeR_queue = new LinkedList<>();
    LinkedList<String> uploadAx_queue = new LinkedList<>();
    LinkedList<String> uploadAy_queue = new LinkedList<>();
    LinkedList<String> uploadAz_queue = new LinkedList<>();
    LinkedList<String> uploadGx_queue = new LinkedList<>();
    LinkedList<String> uploadGy_queue = new LinkedList<>();
    LinkedList<String> uploadGz_queue = new LinkedList<>();
    LinkedList<String> uploadRx_queue = new LinkedList<>();
    LinkedList<String> uploadRy_queue = new LinkedList<>();
    LinkedList<String> uploadRz_queue = new LinkedList<>();
    LinkedList<String> upload_queue = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        acc = findViewById(R.id.acc);
        gyr = findViewById(R.id.gry);
        accText = findViewById(R.id.accText);
        gyrText = findViewById(R.id.gyrText);
        rotText = findViewById(R.id.rotText);

        response = findViewById(R.id.respone);
        status = findViewById(R.id.staus);
        queue = findViewById(R.id.queue);
        upload = findViewById(R.id.upload);
        error = findViewById(R.id.error);
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
                sendFlag = false;
                errorFlag = false;
                stopFlag = false;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Close");
                response.setText("waiting...");
                startFlag = false;
                uploadFlag = false;
                sendFlag = true;
                errorFlag = false;
                stopFlag = true;
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Pause");
                startFlag = false;
                uploadFlag = true;
                sendFlag = false;
                errorFlag = false;
                stopFlag = false;
            }
        });

        //設定定時要執行的方法
        handler.removeCallbacks(updateTimer);
        //設定Delay的時間
        handler.postDelayed(updateTimer, speed);

        //執行上傳
        new Thread(uploadData).start();
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

        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private float fixCalibration(float calibratedR, float calibrationR) {
        if (calibratedR < -1 || calibratedR > 1) {
            calibratedR = -calibratedR + calibrationR;
        }
        return calibratedR;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(mAccelerometer) && matchPoint) {

            // 取得時間數據
            long timeA = System.currentTimeMillis() + offset;
            timerA = dfm.format(new Timestamp(timeA));
            if (showTime)
                Log.d("[Time_Acc]", "EveryTimeA: " + timerA);// + "." + (timeA % 1000));

            if (!preTimeA.equals(timerA)) {
                if (startFlag) {
                    uploadTime_queue.offer(preTimeA);
                    uploadAx_queue.offer(strAx);
                    uploadAy_queue.offer(strAy);
                    uploadAz_queue.offer(strAz);
                }
                // Log.d("[Time_upload]", "[uploadTime]: " + preTimeA);

                endTimeA = (int) (System.currentTimeMillis() + offset);
                TimeA = endTimeA - startTimeA;
                //Log.d("[Time_Acc]", "\t\tendTimeA: " + endTimeA);
                //Log.d("[Time_Acc]", "\t\tTimeA: " + TimeA);
                if (showCount)
                    Log.d("[Count_Acc]", "\tcountA All: " + countA);
                //Log.d("[Value_Acc]", "Acc - \nX: " + strAx + "\n" + "Y: " + strAy + "\n" + "Z: " + strAz + "\n");

                countA = 0;
                uploadA = 1;
                preTimeA = timerA;
            }

            if (countA == 0) {
                strAx = "";
                strAy = "";
                strAz = "";
                startTimeA = (int) (System.currentTimeMillis() + offset);
                //Log.d("[Time_Acc]", "\tstartTimeA: " + startTimeA);
            }

            countA++;
            //Log.d("[Count_Acc]", "\tcountA: " + countA);

            // 移除重力
            for (int i = 0; i < 3; i++) {
                gravity[i] = (float) (0.1 * event.values[i] + 0.9 * gravity[i]);
                motion[i] = event.values[i] - gravity[i];
            }

            // 舊方法
            valueAx = (float) Math.round(motion[0] * 100000) / 100000;
            valueAy = (float) Math.round(motion[1] * 100000) / 100000;
            valueAz = (float) Math.round(motion[2] * 100000) / 100000;
            strAx = String.valueOf(valueAx);
            strAy = String.valueOf(valueAy);
            strAz = String.valueOf(valueAz);

            // 新方法
            strAx = String.valueOf(event.values[0] + TestActivity.calibrationA[0]);
            strAy = String.valueOf(event.values[1] + TestActivity.calibrationA[1]);
            strAz = String.valueOf(event.values[2] + TestActivity.calibrationA[2]);
        }

        if (event.sensor.equals(mGyroscope) && matchPoint) {
            // 取得時間數據
            long timeG = System.currentTimeMillis() + offset;
            timerG = dfm.format(new Timestamp(timeG));
            if (showTime)
                Log.d("[Time_Gyr]", "EveryTimeG: " + timerG);// + "." + (timeG % 1000));

            if (!preTimeG.equals(timerG)) {
                if (startFlag) {
                    uploadTimeG_queue.offer(preTimeA);
                    uploadGx_queue.offer(strGx);
                    uploadGy_queue.offer(strGy);
                    uploadGz_queue.offer(strGz);
                }

                endTimeG = (int) (System.currentTimeMillis() + offset);
                TimeG = endTimeG - startTimeG;
                //Log.d("[Time_Gyr]", "\t\tendTimeG: " + endTimeG);
                //Log.d("[Time_Gyr]", "\t\tTimeG: " + TimeG);
                if (showCount)
                    Log.d("[Count_Gyr]", "\tcountG All: " + countG);
                //Log.d("[Value_Gyr]", "Gyr - \nX: " + strGx + "\n" + "Y: " + strGy + "\n" + "Z: " + strGz + "\n");

                countG = 0;
                uploadG = 1;
                preTimeG = timerG;
            }

            if (countG == 0) {
                strGx = "";
                strGy = "";
                strGz = "";
                startTimeG = (int) (System.currentTimeMillis() + offset);
                //Log.d("[Time_Gyr]", "\tstartTimeG: " + startTimeG);
            }

            countG++;
            //Log.d("[Count_Gyr]", "\tcountG: " + countG);

            if (timestamp != 0) {
                // event.timesamp表示當前的時間，單位是納秒（1百萬分之一毫秒）
                final float dT = (event.timestamp - timestamp) * NS2S;
                angle[0] = event.values[0] * dT;
                angle[1] = event.values[1] * dT;
                angle[2] = event.values[2] * dT;
            }
            timestamp = event.timestamp;

            // 舊方法
            valueGx = (float) Math.round(angle[0] * 1000000) / 1000000;
            valueGy = (float) Math.round(angle[1] * 1000000) / 1000000;
            valueGz = (float) Math.round(angle[2] * 1000000) / 1000000;
            strGx = String.valueOf(valueGx);
            strGy = String.valueOf(valueGy);
            strGz = String.valueOf(valueGz);

            // 新方法
            strGx = String.valueOf(event.values[0] + TestActivity.calibrationG[0]);
            strGy = String.valueOf(event.values[1] + TestActivity.calibrationG[1]);
            strGz = String.valueOf(event.values[2] + TestActivity.calibrationG[2]);
        }

        if (event.sensor.equals(mSensor) && matchPoint) {

            // 取得時間數據
            long timeR = System.currentTimeMillis() + offset;
            timerR = dfm.format(new Timestamp(timeR));
            if (showTime)
                Log.d("[Time_Rot]", "EveryTimeR: " + timerR);// + "." + (timeR % 1000));

            if (!preTimeR.equals(timerR)) {
                if (startFlag) {
                    uploadTimeR_queue.offer(preTimeA);
                    uploadRx_queue.offer(strRx);
                    uploadRy_queue.offer(strRy);
                    uploadRz_queue.offer(strRz);
                }
                //Log.d("[Time_upload]", "[uploadTime]: " + preTimeR);

                endTimeR = (int) (System.currentTimeMillis() + offset);
                TimeR = endTimeR - startTimeR;
                //Log.d("[Time_Rot]", "\t\tendTimeR: " + endTimeR);
                //Log.d("[Time_Rot]", "\t\tTimeR: " + TimeR);
                if (showCount)
                    Log.d("[Count_Rot]", "\tcountR All: " + countR);
                //Log.d("[Value_Rot]", "Rot - \nX: " + strRx + "\n" + "Y: " + strRy + "\n" + "Z: " + strRz + "\n");

                countR = 0;
                uploadR = 1;
                preTimeR = timerR;
            }

            if (countR == 0) {
                strRx = "";
                strRy = "";
                strRz = "";
                startTimeR = (int) (System.currentTimeMillis() + offset);
                //Log.d("[Time_Rot]", "\tstartTimeR: " + startTimeR);
            }

            countR++;
            //Log.d("[Count_Rot]", "\tcountR: " + countR);

            // 舊方法
            strRx = String.valueOf(event.values[0] + TestActivity.calibrationR[0]);
            strRy = String.valueOf(event.values[1] + TestActivity.calibrationR[1]);
            strRz = String.valueOf(event.values[2] + TestActivity.calibrationR[2]);

            // 新方法
            valueRx = event.values[0] + TestActivity.calibrationR[0];
            valueRy = event.values[1] + TestActivity.calibrationR[1];
            valueRz = event.values[2] + TestActivity.calibrationR[2];

            strRx = String.valueOf(fixCalibration(valueRx, TestActivity.calibrationR[0]));
            strRy = String.valueOf(fixCalibration(valueRy, TestActivity.calibrationR[1]));
            strRz = String.valueOf(fixCalibration(valueRz, TestActivity.calibrationR[2]));
        }

        acc.setText("Acc count: " + countA);
        gyr.setText("Gyr count: " + countG);

        accText.setText("Ax:" + strAx + "\n" + "Ay:" + strAy + "\n" + "Az:" + strAz + "\n");
        gyrText.setText("Gx:" + strGx + "\n" + "Gy:" + strGy + "\n" + "Gz:" + strGz + "\n");
        rotText.setText("Rx:" + strRx + "\n" + "Ry:" + strRy + "\n" + "Rz:" + strRz + "\n");
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

            String queueSize = uploadTime_queue.size() + "\n";
            queueSize += uploadAx_queue.size() + "/" + uploadAy_queue.size() + "/" + uploadAz_queue.size() + "\n";
            queueSize += uploadGx_queue.size() + "/" + uploadGy_queue.size() + "/" + uploadGz_queue.size() + "\n";
            queueSize += uploadRx_queue.size() + "/" + uploadRy_queue.size() + "/" + uploadRz_queue.size() + "\n";
            //queueSize += countA + "/" + countG + "/" + countR;

            //queue.setText("Queue size: " + uploadTime_queue.size());
            queue.setText("Queue size: " + queueSize);
            upload.setText("Upload size: " + uploadIndex);

            matchPoint = true;

            handler.postDelayed(this, speed);
        }
    };
    private Runnable uploadData = new Runnable() {
        @Override
        public void run() {
            try {
                String file_name = "//sdcard//testfile.txt";

                File file = new File(file_name);
                if (!file.exists()) {
                    file.createNewFile();
                }
                // 覆蓋檔案
                //OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");// 覆蓋檔案
                // 追加檔案
                OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"); // 追加檔案
                BufferedWriter writer = new BufferedWriter(os);

                while (true) {
                    if (uploadFlag) {
                        //Log.d("[Upload]", "<SEND!!!!!>");

                        //Log.d("[Upload]", "<Queue> " + uploadTime_queue.size());
                        if (uploadTime_queue.size() >= saveSize) {
                            saveFlag = true;
                        }

                        while (saveFlag) {
                            // 取出佇列數據
                            if (uploadTime_queue.size() > 0) {
                                //Log.d("[Upload]", "<Queue-B> " + uploadAx_queue.size() + "/" + uploadAy_queue.size() + "/" + uploadAz_queue.size());
                                uploadTime = uploadTime_queue.poll();
                                uploadAx = uploadAx_queue.poll();
                                uploadAy = uploadAy_queue.poll();
                                uploadAz = uploadAz_queue.poll();
                                //Log.d("[Upload]", "<Queue-A> " + uploadAx_queue.size() + "/" + uploadAy_queue.size() + "/" + uploadAz_queue.size());
                                //Log.d("[Upload]", "<Queue-S> " + uploadAx + "/" + uploadAy + "/" + uploadAz);
                            } else {
                                uploadTime = null;
                                uploadAx = null;
                                uploadAy = null;
                                uploadAz = null;
                            }
                            if (uploadTime != null && !uploadTime.equals("")) {
                                if (uploadTimeG != null) {
                                    if (uploadTimeG.equals(""))
                                        uploadTimeG = uploadTimeG_queue.poll();
                                    if (Double.parseDouble(uploadTime.substring(17)) >= Double.parseDouble(uploadTimeG.substring(17))) {
                                        uploadGx = uploadGx_queue.poll();
                                        uploadGy = uploadGy_queue.poll();
                                        uploadGz = uploadGz_queue.poll();
                                        if (uploadTimeG_queue.size() > 0)
                                            uploadTimeG = uploadTimeG_queue.poll();
                                    } else {
                                        uploadGx = null;
                                        uploadGy = null;
                                        uploadGz = null;
                                    }
                                }
                                if (uploadTimeR != null) {
                                    if (uploadTimeR.equals(""))
                                        uploadTimeR = uploadTimeR_queue.poll();
                                    if (Double.parseDouble(uploadTime.substring(17)) >= Double.parseDouble(uploadTimeR.substring(17))) {
                                        uploadRx = uploadRx_queue.poll();
                                        uploadRy = uploadRy_queue.poll();
                                        uploadRz = uploadRz_queue.poll();
                                        if (uploadTimeR_queue.size() > 0)
                                            uploadTimeR = uploadTimeR_queue.poll();
                                    } else {
                                        uploadRx = null;
                                        uploadRy = null;
                                        uploadRz = null;
                                    }
                                }
                            }
                            if (uploadTime != null && uploadAx != null && uploadGx != null) {
                            //if (uploadTime != null && uploadAx != null && uploadGx != null && uploadRx != null) {
                                uploadIndex += 1;
                                savedSize += 1;
                                String saveText = uploadIndex + "," + uploadTime + "," +
                                        uploadAx + "," + uploadAy + "," + uploadAz + "," +
                                        uploadGx + "," + uploadGy + "," + uploadGz + "," +
                                        uploadRx + "," + uploadRy + "," + uploadRz + "," + phoneNum + "\n";
                                writer.write(saveText);
                                Log.d("[File Out]", "<Success> " + saveText);
                            }

                            if (savedSize >= saveSize) {
                                saveFlag = false;
                                savedSize = 0;
                                break;
                            }
                        }
                    } else if (stopFlag){
                        writer.close();
                        Log.d("[File Out]", "<Close>");
                        break;
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                error.setText("Error1: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                error.setText("Error: " + e.getMessage() + "\n" + e.getLocalizedMessage());
            }
        }
    };
}
