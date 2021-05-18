package com.example.motionsensors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class TestActivity extends AppCompatActivity implements SensorEventListener {
    String TAG = "Sntp";

    private WebView webView;

    EditText phoneText;
    TextView offsetText;
    TextView desText;
    TextView sensorText;
    Button launch;

    long diff;
    static long offset;
    static int phoneNum = 0;

    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    String strX = "";
    String strY = "";
    String strZ = "";
    private SensorManager sensorManager;
    private Sensor msensor;
    public float[] value = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String responseHtml = MainActivity.myResponse;
        if (!responseHtml.equals("")) {
            webView = new WebView(this);
            webView.setWebViewClient(new WebViewClient());
            webView.loadData(responseHtml, "text/HTML", "UTF-8");
            setContentView(webView);

        } else {
            setContentView(R.layout.activity_time);
            phoneText = findViewById(R.id.phoneSet);
            offsetText = findViewById(R.id.offset);
            desText = findViewById(R.id.description);
            sensorText = findViewById(R.id.sensor);
            launch = findViewById(R.id.launch);

            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            msensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

            desText.setText("Description: \n" +
                    "2: sony\n" +
                    "3: M8\n" +
                    "4: mi");

            launch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    phoneNum = Integer.parseInt(phoneText.getText().toString());
                    Intent intent = new Intent();
                    intent.setClass(TestActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            Thread getNTP = new Thread() {
                @Override
                public void run() {
                    SntpClient sntpClient = new SntpClient();

                    if (sntpClient.requestTime("tw.pool.ntp.org", 30000)) {
                        long now = System.currentTimeMillis();
                        Timestamp nowTime = new Timestamp(now);

                        long ntp = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
                        Date ntpTime = new Date(ntp);

                        offset = sntpClient.getOffset();
                        diff = ntp - now;

                        Log.d(TAG, "[ntp]: " + ntp);
                        Log.d(TAG, "[ntp]: " + dfm.format(ntpTime));

                        Log.d(TAG, "[now]: " + now);
                        Log.d(TAG, "[now]: " + dfm.format(nowTime));
                    }
                }
            };

            try {
                getNTP.start();
                getNTP.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            offsetText.setText("NTP offset = " + offset + "\n\nDiff = " + diff);
            Log.d(TAG, "[Diff]: " + offset);
            Log.d(TAG, "[Diff]: " + diff);

            fileTest(1);
            fileTest(3);
            fileTest(2);
            fileTest(3);

            Log.d(TAG, "[END]");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(msensor)) {
            // 移除重力
            for (int i = 0; i < 3; i++) {
                value[i] = event.values[i];
            }

            strX = String.valueOf(Math.round(value[0] * 100.0) / 100.0);
            strY = String.valueOf(Math.round(value[1] * 100.0) / 100.0);
            strZ = String.valueOf(Math.round(value[2] * 100.0) / 100.0);

            sensorText.setText("x: " + strX + "\n" + "y: " + strY + "\n" + "z: " + strZ + "\n");

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void fileTest(int set) {
        switch (set) {
            case 1:
                boolean mExternalStorageAvailable = false;
                boolean mExternalStorageWriteable = false;
                String state = Environment.getExternalStorageState();

                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    // 我們可以對外部儲存空間進行讀取跟寫入的動作
                    mExternalStorageAvailable = mExternalStorageWriteable = true;
                } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                    // 我們只能讀取外部儲存空間
                    mExternalStorageAvailable = true;
                    mExternalStorageWriteable = false;
                } else {
                    // 外部儲存空間錯誤，錯誤的情況很多，但一般我們只關心讀取跟寫入的狀態
                    mExternalStorageAvailable = mExternalStorageWriteable = false;
                }
                Log.d(TAG, "[File Read]" + mExternalStorageAvailable);
                Log.d(TAG, "[File Write]" + mExternalStorageWriteable);
                break;
            case 2:
                try {
                    String file_name = "//sdcard//testfile.txt";
                    String file_content = "";

                    File file = new File(file_name);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    // 覆蓋檔案
                    OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");// 覆蓋檔案
                    // 追加檔案
                    //OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"); // 追加檔案
                    BufferedWriter writer = new BufferedWriter(os);
                    writer.write(file_content);
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    String file_name = "//sdcard//testfile.txt";
                    String file_content = "";
                    String line;

                    File file = new File(file_name);
                    if (file.isFile() && file.exists()) {
                        // 讀取檔案
                        FileInputStream fis = new FileInputStream(file);
                        InputStreamReader sr = new InputStreamReader(fis, "UTF-8");
                        BufferedReader br = new BufferedReader(sr);
                        // 印出檔案
                        while ((line = br.readLine()) != null) {
                            file_content += line + "\n";
                        }
                        sr.close();
                        Log.d(TAG, "[File In]: \n" + file_content);
                    } else {
                        Log.d(TAG, "[File]: Not exist!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}

class SntpClient {
    private static final String TAG = "Sntp";
    private static final boolean DBG = true;

    private static final int REFERENCE_TIME_OFFSET = 16;
    private static final int ORIGINATE_TIME_OFFSET = 24;
    private static final int RECEIVE_TIME_OFFSET = 32;
    private static final int TRANSMIT_TIME_OFFSET = 40;
    private static final int NTP_PACKET_SIZE = 48;

    private static final int NTP_PORT = 123;
    private static final int NTP_MODE_CLIENT = 3;
    private static final int NTP_MODE_SERVER = 4;
    private static final int NTP_MODE_BROADCAST = 5;
    private static final int NTP_VERSION = 3;

    private static final int NTP_LEAP_NOSYNC = 3;
    private static final int NTP_STRATUM_DEATH = 0;
    private static final int NTP_STRATUM_MAX = 15;

    // Number of seconds between Jan 1, 1900 and Jan 1, 1970
    // 70 years plus 17 leap days
    private static final long OFFSET_1900_TO_1970 = ((365L * 70L) + 17L) * 24L * 60L * 60L;

    // system time computed from NTP server response
    private long mNtpTime;

    // value of SystemClock.elapsedRealtime() corresponding to mNtpTime
    private long mNtpTimeReference;

    // round trip time in milliseconds
    private long mRoundTripTime;

    // TODO: ADD
    private long offset;

    private static class InvalidServerReplyException extends Exception {
        public InvalidServerReplyException(String message) {
            super(message);
        }
    }

    /**
     * Sends an SNTP request to the given host and processes the response.
     *
     * @param host    host name of the server.
     * @param timeout network timeout in milliseconds.
     * @return true if the transaction was successful.
     */
    public boolean requestTime(String host, int timeout) {
        DatagramSocket socket = null;
        InetAddress address = null;
        try {
            address = InetAddress.getByName(host);
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            byte[] buffer = new byte[NTP_PACKET_SIZE];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, NTP_PORT);

            // set mode = 3 (client) and version = 3
            // mode is in low 3 bits of first byte
            // version is in bits 3-5 of first byte
            buffer[0] = NTP_MODE_CLIENT | (NTP_VERSION << 3);

            // get current time and write it to the request packet
            final long requestTime = System.currentTimeMillis();
            final long requestTicks = SystemClock.elapsedRealtime();
            writeTimeStamp(buffer, TRANSMIT_TIME_OFFSET, requestTime);

            socket.send(request);

            // read the response
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
            final long responseTicks = SystemClock.elapsedRealtime();
            final long responseTime = requestTime + (responseTicks - requestTicks);

            // extract the results
            final byte leap = (byte) ((buffer[0] >> 6) & 0x3);
            final byte mode = (byte) (buffer[0] & 0x7);
            final int stratum = (int) (buffer[1] & 0xff);
            final long originateTime = readTimeStamp(buffer, ORIGINATE_TIME_OFFSET);
            final long receiveTime = readTimeStamp(buffer, RECEIVE_TIME_OFFSET);
            final long transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET);

            /* do sanity check according to RFC */
            // TODO: validate originateTime == requestTime.
            checkValidServerReply(leap, mode, stratum, transmitTime);

            long roundTripTime = responseTicks - requestTicks - (transmitTime - receiveTime);
            // receiveTime = originateTime + transit + skew
            // responseTime = transmitTime + transit - skew
            long clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime)) / 2;
            //             = ((originateTime + transit + skew - originateTime) +
            //                (transmitTime - (transmitTime + transit - skew)))/2
            //             = ((transit + skew) + (transmitTime - transmitTime - transit + skew))/2
            //             = (transit + skew - transit + skew)/2
            //             = (2 * skew)/2 = skew
            Log.d(TAG, "Request time form ntp server success, " + address.toString() + " ,roundTripTime: " + roundTripTime);
            if (DBG) {
                Log.d(TAG, "round trip: " + roundTripTime + "ms, " +
                        "clock offsetText: " + clockOffset + "ms");
            }

            // save our results - use the times on this side of the network latency
            // (response rather than request time)
            mNtpTime = responseTime + clockOffset;
            mNtpTimeReference = responseTicks;
            mRoundTripTime = roundTripTime;
            offset = clockOffset;
        } catch (Exception e) {
//            if (DBG) {
//                Log.e(TAG, "Error address: " + address.toString());
//            }
            Log.e(TAG, "Request time from ntp server failed ,msg: " + e.getMessage());
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        return true;
    }

    /**
     * Returns the time computed from the NTP transaction.
     *
     * @return time value computed from NTP server response.
     */
    public long getNtpTime() {
        return mNtpTime;
    }

    /**
     * Returns the reference clock value (value of SystemClock.elapsedRealtime())
     * corresponding to the NTP time.
     *
     * @return reference clock corresponding to the NTP time.
     */
    public long getNtpTimeReference() {
        return mNtpTimeReference;
    }

    /**
     * Returns the offsetText time of the NTP transaction
     *
     * @return offsetText time in milliseconds.
     */
    // TODO: ADD
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the round trip time of the NTP transaction
     *
     * @return round trip time in milliseconds.
     */
    public long getRoundTripTime() {
        return mRoundTripTime;
    }

    private static void checkValidServerReply(
            byte leap, byte mode, int stratum, long transmitTime)
            throws InvalidServerReplyException {
        if (leap == NTP_LEAP_NOSYNC) {
            throw new InvalidServerReplyException("unsynchronized server");
        }
        if ((mode != NTP_MODE_SERVER) && (mode != NTP_MODE_BROADCAST)) {
            throw new InvalidServerReplyException("untrusted mode: " + mode);
        }
        if ((stratum == NTP_STRATUM_DEATH) || (stratum > NTP_STRATUM_MAX)) {
            throw new InvalidServerReplyException("untrusted stratum: " + stratum);
        }
        if (transmitTime == 0) {
            throw new InvalidServerReplyException("zero transmitTime");
        }
    }

    /**
     * Reads an unsigned 32 bit big endian number from the given offsetText in the buffer.
     */
    private long read32(byte[] buffer, int offset) {
        byte b0 = buffer[offset];
        byte b1 = buffer[offset + 1];
        byte b2 = buffer[offset + 2];
        byte b3 = buffer[offset + 3];

        // convert signed bytes to unsigned values
        int i0 = ((b0 & 0x80) == 0x80 ? (b0 & 0x7F) + 0x80 : b0);
        int i1 = ((b1 & 0x80) == 0x80 ? (b1 & 0x7F) + 0x80 : b1);
        int i2 = ((b2 & 0x80) == 0x80 ? (b2 & 0x7F) + 0x80 : b2);
        int i3 = ((b3 & 0x80) == 0x80 ? (b3 & 0x7F) + 0x80 : b3);

        return ((long) i0 << 24) + ((long) i1 << 16) + ((long) i2 << 8) + (long) i3;
    }

    /**
     * Reads the NTP time stamp at the given offsetText in the buffer and returns
     * it as a system time (milliseconds since January 1, 1970).
     */
    private long readTimeStamp(byte[] buffer, int offset) {
        long seconds = read32(buffer, offset);
        long fraction = read32(buffer, offset + 4);
        // Special case: zero means zero.
        if (seconds == 0 && fraction == 0) {
            return 0;
        }
        return ((seconds - OFFSET_1900_TO_1970) * 1000) + ((fraction * 1000L) / 0x100000000L);
    }

    /**
     * Writes system time (milliseconds since January 1, 1970) as an NTP time stamp
     * at the given offsetText in the buffer.
     */
    private void writeTimeStamp(byte[] buffer, int offset, long time) {
        // Special case: zero means zero.
        if (time == 0) {
            Arrays.fill(buffer, offset, offset + 8, (byte) 0x00);
            return;
        }

        long seconds = time / 1000L;
        long milliseconds = time - seconds * 1000L;
        seconds += OFFSET_1900_TO_1970;

        // write seconds in big endian dfm2
        buffer[offset++] = (byte) (seconds >> 24);
        buffer[offset++] = (byte) (seconds >> 16);
        buffer[offset++] = (byte) (seconds >> 8);
        buffer[offset++] = (byte) (seconds >> 0);

        long fraction = milliseconds * 0x100000000L / 1000L;
        // write fraction in big endian dfm2
        buffer[offset++] = (byte) (fraction >> 24);
        buffer[offset++] = (byte) (fraction >> 16);
        buffer[offset++] = (byte) (fraction >> 8);
        // low order bits should be random data
        buffer[offset++] = (byte) (Math.random() * 255.0);
    }
}