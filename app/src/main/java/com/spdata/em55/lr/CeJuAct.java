package com.spdata.em55.lr;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.serialport.DeviceControl;
import android.serialport.SerialPort;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.spdata.em55.R;
import com.spdata.em55.base.BaseAct;
import com.spdata.em55.lr.view.WaitingBar;
import com.spdata.em55.px.psam.utils.DataConversionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by suntianwei on 2017/2/8.
 */

public class CeJuAct extends BaseAct implements View.OnClickListener {
    //发送命令 单次测距：    AT1#
    byte[] cmd_single = new byte[]{0x41, 0x54, 0x31, 0x23};
    byte[] send = new byte[]{0x41, 0x54, 0x47, 0x23};//ATG# 初始化设备
    Button btnSingle, btnClear, btnSwitch;
    private ToggleButton btnAuto;
    private ImageView imgtop, imgbottom;
    private TextView tvResult, tvStatus;
    private DeviceControl control;
    private SerialPort mSerialPort;
    private int fd;
    private boolean isTop = true;
    private EditText edvRecord;
    float results = 0;

    private final String TAG = "RedDATA";
    private WaitingBar bar;
    private GoogleApiClient client;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_ceju);
        initUI();
        initDevice();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initDevice() {
        try {
            mSerialPort = new SerialPort();
            mSerialPort.OpenSerial(SerialPort.SERIAL_TTYMT1, 9600);//kt55串口
            control = new DeviceControl(DeviceControl.PowerType.MAIN_AND_EXPAND, 73, 0);
            control.PowerOnDevice();
            fd = mSerialPort.getFd();
            byte[] bytes = mSerialPort.ReadSerial(fd, 1024);
            if (!Arrays.equals(bytes, ff)) {
                for (int i = 0; i < 19; i++) {
                    mSerialPort.WriteSerialByte(fd, send);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ReadSerialThread readSerialThread;
    byte aa[] = {65, 84, 49, 35};//AT1#
    byte bb[] = {65, 84, 51, 35};//AT3#
    byte cc[] = {65, 84, 88, 35};//atx#
    byte dd[] = {65, 84, 51, 35, 65, 84, 49, 35};
    byte ee[] = {65, 84, 69, 35};//ate# jiq机器发生错误
    byte ff[] = {65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71,
            35, 65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71,
            35, 65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71,
            35, 65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71,
            35, 65, 84, 71, 35, 65, 84, 71, 35, 65, 84, 71, 35};//atG#  仪器上电复位后,会发ATG# 19次，请上位机收到ATG# 后，才能认为数据有效
    /**
     * 数据处理
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            byte[] data = (byte[]) msg.obj;

            List<String> results = parseData(data);
            for (String result : results) {
                String temp = edvRecord.getText().toString();
                play(2, 0);

                if (temp.length() > 10) {
                    edvRecord.setText(result + "M\n");

                } else {
                    edvRecord.append(result + "M\n");
                }
                tvResult.setText(result + "M");
            }
        }
    };
    boolean isStart = true;

    private void initUI() {
        btnSingle = (Button) findViewById(R.id.btn_send);
        btnAuto = (ToggleButton) findViewById(R.id.btn_zid);
        btnClear = (Button) findViewById(R.id.btn_clear);
        edvRecord = (EditText) findViewById(R.id.edv_age);
        imgbottom = (ImageView) findViewById(R.id.img_phone_b);
        imgtop = (ImageView) findViewById(R.id.imageView2);
        tvResult = (TextView) findViewById(R.id.tv_result);
        btnSwitch = (Button) findViewById(R.id.btn_select);
        bar = (WaitingBar) findViewById(R.id.waitingBar);
        tvStatus = (TextView) findViewById(R.id.textView2);
        bar.setVisibility(View.GONE);
        btnSwitch.setOnClickListener(this);
        btnSingle.setOnClickListener
                (new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         isStart = true;
                         mSerialPort.clearPortBuf(fd);
                         mSerialPort.WriteSerialByte(fd, cmd_single);//发送测距命令
                         readSerialThread = new ReadSerialThread();
                         readSerialThread.start();
                     }
                 }

                );
        btnAuto.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {
                     @Override
                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                         if (isChecked) {
                             //开始连续测距
                             isStart = true;
                             mSerialPort.clearPortBuf(fd);
                             startTask();
                             readSerialThread = new ReadSerialThread();
                             readSerialThread.start();
                             tvStatus.setText("接收数据中……");
                             bar.setVisibility(View.VISIBLE);
                         } else {
                             //Stop
//                             mSerialPort.clearPortBuf(fd);
                             readSerialThread.interrupt();
                             if (timer != null) {
                                 timer.cancel();
                                 timer = null;
                             }
                             bar.setVisibility(View.INVISIBLE);
                             tvStatus.setText("");
                         }
                     }
                 }

                );
        btnClear.setOnClickListener(this);
    }

    private void startTask() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mSerialPort.WriteSerialByte(fd, cmd_single);
//                    SystemClock.sleep(200);
//                    try {
//                        byte[] bytes = mSerialPort.ReadSerial(mSerialPort.getFd(), 1024);
//                        if (bytes != null) {
//                            String log = "";
//                            for (byte x : bytes) {
//                                log += String.format("0x%x", x);
//                            }
//                            Log.d(TAG, "Read_length=" + log);
//                            Message msg = new Message();
//                            msg.obj = bytes;
//                            handler.sendMessage(msg);
//                        }
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
                }
            }, 0, 1000);
        }
    }

    /**
     * 计算长度
     *
     * @param data
     * @return
     */
    private String convertValue(byte[] data) {
        int number = byteArrayToInt(data);
        if (isTop) {
            results = (float) ((number - 650) / 10000.0);//从顶部开始计算
        } else {
            results = (float) ((number + 1000) / 10000.0);//从底部开始计算
        }
        //四舍五入
        DecimalFormat df = new DecimalFormat("#,##0.00");
        String dff = df.format(results);
        return dff;
    }

    /**
     * 解析串口原始数据
     *
     * @param data
     * @return
     */
    private List<String> parseData(byte[] data) {
        List<String> result = new ArrayList<>();

        if (data.length < 8) {
            Log.e(TAG, "====parseData len error" + DataConversionUtils.byteArrayToStringLog(data,
                    data.length));
            if (Arrays.equals(data, aa)) {
                isStart = false;
            } else {
                mSerialPort.WriteSerialByte(fd, cmd_single);//发送测距命令
            }
            if (Arrays.equals(data, ee)) {
                edvRecord.setText(result + "err\n");
            }

            return result;
        }
        for (int i = 0; i < data.length; i++) {
            try {
                if ((byte) data[i] == 0x041 && (byte) data[i + 7] == 0x023) {
                    byte[] temp = new byte[8];

                    System.arraycopy(data, i, temp, 0, 8);
                    //加法和
                    byte sum = (byte) (temp[3] + temp[4] + temp[5]);
                    byte[] values = new byte[3];
                    System.arraycopy(temp, 3, values, 0, 3);
                    //判断校验
                    if (sum == temp[6]) {
                        String object = convertValue(values);
                        result.add(object);
                        Log.d(TAG, "====parseData add" + object);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;

    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("CeJuAct Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    /**
     * 读串口线程
     */
    private class ReadSerialThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
            try {
                SystemClock.sleep(200);
                byte[] bytes = mSerialPort.ReadSerial(mSerialPort.getFd(), 1024);
                if (bytes != null) {
                    String log = "";
                    for (byte x : bytes) {
                        log += String.format("0x%x", x);
                    }
                    Log.d(TAG, "Read_length=" + log);
                    Message msg = new Message();
                    msg.obj = bytes;
                    handler.sendMessage(msg);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (readSerialThread != null) {
                readSerialThread.interrupt();
                readSerialThread = null;
            }
            if (timer!=null) {
                timer.cancel();
                timer=null;
            }
            mSerialPort.CloseSerial(fd);
            control.PowerOffDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear:
                edvRecord.setText("");
                tvResult.setText("0.00 M ");
                break;
            case R.id.btn_select:
                if (isTop) {
                    isTop = false;
                    Toast.makeText(this, "底部开始计算", Toast.LENGTH_SHORT).show();
                    imgtop.setVisibility(View.GONE);
                    imgbottom.setVisibility(View.VISIBLE);
                } else {
                    isTop = true;
                    Toast.makeText(this, "顶部开始计算", Toast.LENGTH_SHORT).show();
                    imgtop.setVisibility(View.VISIBLE);
                    imgbottom.setVisibility(View.GONE);
                }
                break;
        }
    }

    /**
     * byte[]转int
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < bytes.length; i++) {
            int shift = (bytes.length - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }
}


