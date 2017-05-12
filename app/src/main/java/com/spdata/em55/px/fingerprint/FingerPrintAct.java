package com.spdata.em55.px.fingerprint;

//------------------------------------------------------------------------------------------

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.DeviceControl;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalpersona.uareu.Fmd;
import com.mylibrary.FingerManger;
import com.mylibrary.inf.IFingerPrint;
import com.mylibrary.inf.MsgCallBack;
import com.mylibrary.ulits.Data;
import com.spdata.em55.R;
import com.spdata.em55.base.BaseAct;
import com.spdata.em55.px.print.utils.ApplicationContext;

import java.io.IOException;

//------------------------------------------------------------------------------------------

public class FingerPrintAct extends BaseAct implements View.OnClickListener {
    Button btnOpen, btnGetImage, btnGetQuality, btnColse, btnCompare,
            btnCreateTemplate, btnEnroll, btnSearch;
    ImageView fingerImage = null;
    TextView tvMsg, tvNum;
    private IFingerPrint iFingerPrint = null;
    DeviceControl deviceControl;
    DeviceControl deviceContro2;
    private String sss = "";
    private String ssss = "";
    Fmd fmd1 = null;
    Fmd fmd2 = null;
    private byte[] template1;
    private byte[] template2;
    //    int template = 1;
    boolean template = true;
    String TAG = "finger";
    int flg = 0;
    String s1 = "";
    String s2 = "";
    private Dialog dialog;
    int ii = 0;

    Context mContext;
    Activity mActivity;
    private long now;
    private long start;
    Bitmap bitmap = null;
    private Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger);
        ApplicationContext.getInstance().addActivity(FingerPrintAct.this);
        initGUI();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.hall.success");
        registerReceiver(receiver, intentFilter);
        try {
            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN_AND_EXPAND, 63, 5, 6);
//            deviceContro2 = new DeviceControl(DeviceControl.PowerType.MAIN, 128);
            deviceControl.PowerOnDevice();
//            deviceContro2.PowerOnDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initGUI() {
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        tvNum = (TextView) findViewById(R.id.tv_num);
        fingerImage = (ImageView) findViewById(R.id.btn_imageView);
        btnOpen = (Button) findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(this);
        btnGetImage = (Button) findViewById(R.id.btn_getimage);
        btnGetImage.setOnClickListener(this);
        btnGetQuality = (Button) findViewById(R.id.btn_quality);
        btnGetQuality.setOnClickListener(this);
        btnCreateTemplate = (Button) findViewById(R.id.btn_getTemplate);
        btnCreateTemplate.setOnClickListener(this);
        btnCompare = (Button) findViewById(R.id.btn_compare);
        btnCompare.setOnClickListener(this);
        btnColse = (Button) findViewById(R.id.btn_colse);
        btnColse.setOnClickListener(this);
        btnEnroll = (Button) findViewById(R.id.btn_Enroll);
        btnEnroll.setOnClickListener(this);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(this);
        setBtnState(false);

    }

    ProgressDialog mProgressDialog;

    @Override
    protected void onResume() {
        super.onResume();
        start = System.currentTimeMillis();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("搜索指纹模板 ");
        mProgressDialog.setMessage("初始中……");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
//        dialog = CreateDialog.showLoadingDialog(FingerPrintAct.this, "搜索指纹模板");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (iFingerPrint == null) {
                    now = System.currentTimeMillis();
                    iFingerPrint = FingerManger.getIFingerPrintIntance(FingerPrintAct.this, FingerPrintAct.this, handler, new MsgCallBack() {
                        @Override
                        public void callBackInfo(Data data) {
                            handler.sendMessage(handler.obtainMessage(22, data));
                        }
                    });
                    if (now - start > 6000) {
                        finish();
                        break;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (iFingerPrint != null) {
//                            CreateDialog.closeDialog(dialog);
                            mProgressDialog.cancel();
                        } else {
                            finish();
                            Toast.makeText(FingerPrintAct.this, "初始失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 22:
                    data = (Data) msg.obj;
                    if (data.isOpenFlag()) {
                        setBtnState(true);
                        btnOpen.setEnabled(false);
                    } else {
                        setBtnState(false);
                        btnOpen.setEnabled(true);
                        tvMsg.setText(data.getInfoMsg());
                    }
                    if (data.getTcs1gFmd() != null) {

                        if (template) {
//                            template +=1;
                            template = false;
                            fmd1 = data.getTcs1gFmd();
                            Toast.makeText(FingerPrintAct.this, "fmd1", Toast.LENGTH_SHORT).show();
                        } else {
                            template = true;
                            fmd2 = data.getTcs1gFmd();
                            Toast.makeText(FingerPrintAct.this, "fmd2", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (data.getTemplateBytes() != null) {
                        if (template) {
                            template = false;
                            template1 = new byte[1024];
                            template1 = data.getTemplateBytes();
//                    for (int i = 0; i < LAPI.FPINFO_STD_MAX_SIZE; i++) {
//                        sss += String.format("%02x", template1[i]);
//                    }
//                    Log.i(TAG, "handleMessage: " + flg + "\n" + sss);
                            Toast.makeText(FingerPrintAct.this, "template1", Toast.LENGTH_SHORT).show();


                        } else {
                            template = true;
                            template2 = new byte[1024];
                            template2 = data.getTemplateBytes();
//                    for (int i = 0; i < LAPI.FPINFO_STD_MAX_SIZE; i++) {
//                        ssss += String.format("%02x", template2[i]);
//                    }
//                    Log.i(TAG, "handleMessage: " + flg + "\n" + ssss);
                            Toast.makeText(FingerPrintAct.this, "template2", Toast.LENGTH_SHORT).show();
                        }
                    }
                    tvMsg.setText(data.getInfoMsg());
                    fingerImage.setImageBitmap(data.getFingerBitmap());
                    break;
                default:
                    break;
            }
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.hall.success")) {
                try {
                    deviceControl.PowerOnDevice();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction().equals("com.geomobile.hallremove")) {
            }
        }
    };

    private void setBtnState(boolean btnState) {
        btnGetImage.setEnabled(btnState);
        btnGetQuality.setEnabled(btnState);
        btnColse.setEnabled(btnState);
        btnCompare.setEnabled(btnState);
        btnCreateTemplate.setEnabled(btnState);
        btnEnroll.setEnabled(btnState);
        btnSearch.setEnabled(btnState);
    }

    @Override
    public void onClick(View view) {
        if (view == btnOpen) {
            iFingerPrint.openReader();
        } else if (view == btnColse) {
            iFingerPrint.closeReader();
        } else if (view == btnGetImage) {
            iFingerPrint.getImage();


        } else if (view == btnGetQuality) {
            iFingerPrint.getImageQuality();
            tvMsg.setText(data.getInfoMsg());
            tvNum.setText(String.format("质量 = %d", data.getFinferQualitys()));

        } else if (view == btnCreateTemplate) {
            iFingerPrint.createTemplate();

//            tvMsg.setText(data.getInfoMsg());
        } else if (view == btnCompare) {
//            for (int i = 0; i < template1.length; i++) {
//                s1 += String.format("%02x", template1[i]);
//            }
//            for (int i = 0; i < template2.length; i++) {
//                s2 += String.format("%02x", template2[i]);
//            }
//            Log.i(TAG, "MessageS1: " + "\n" + s1);
//            Log.i(TAG, "MessageS2: " + "\n" + s2);
            iFingerPrint.comparisonFinger(template1, template2);
            iFingerPrint.comparisonFinger(fmd1, fmd2);
            iFingerPrint.comparisonFinger();
            tvMsg.setText(data.getInfoMsg());
            tvNum.setText("比对分数：" + data.getComparisonNum() + "");
        } else if (view == btnEnroll) {
            iFingerPrint.enrollment();
        } else if (view == btnSearch) {
            iFingerPrint.searchFinger();
        }
    }


    private static String charToHexString(byte[] val) {
        String temp = "";
        for (int i = 0; i < val.length; i++) {
            String hex = Integer.toHexString(0xff & val[i]);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            temp += hex.toUpperCase();
        }
        return temp;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            deviceControl.PowerOffDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
        if (data != null) {
            data = null;
        }
        fingerImage.refreshDrawableState();
        if (iFingerPrint != null) {
            iFingerPrint.unObject();
            iFingerPrint = null;
        }
        unregisterReceiver(receiver);
        try {
            deviceControl.PowerOffDevice();
//            deviceContro2.PowerOffDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}