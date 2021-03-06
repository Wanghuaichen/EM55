package com.spdata.em55;

import android.os.Bundle;
import android.serialport.DeviceControl;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spdata.em55.base.BaseAct;
import com.spdata.em55.gxandurx.ReadActivity;
import com.spdata.em55.gxandurx.UhfAct;
import com.spdata.em55.lr.DistanceAct;
import com.spdata.em55.lr.GpsAct;
import com.spdata.em55.lr.TemperatureAct;
import com.spdata.em55.px.ID2.ID2Act;
import com.spdata.em55.px.fingerprint.fzid.FpSDKSampleP11MActivity;
import com.spdata.em55.px.fingerprint.tcs1.Tcs1Activity;
import com.spdata.em55.px.fingerprint.tcs1g.UareUSampleJava;
import com.spdata.em55.px.print.print.demo.firstview.ConnectAvtivity;
import com.spdata.em55.px.print.utils.ApplicationContext;
import com.spdata.em55.px.psam.PsamAct;
import com.spdata.em55.view.ProgersssDialog;
import com.spdata.updateversion.UpdateVersion;
import com.spdata.util.FingerTypes;
import com.spdata.util.FlippingLoadingDialog;

import java.io.IOException;

import static com.spdata.util.FingerTypes.getrwusbdevices;


/**
 * Created by lenovo_pc on 2016/9/27.
 */

public class MenuAct extends BaseAct implements View.OnClickListener {
    LinearLayout lygps, lywendu, lyceju, lyupdata, lyinfrared;
    LinearLayout layoutid, layoutpasm, layoutprint, layoutfinger, lyUhf;
    TextView tvversion;
    private final String TAG = "state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_menu);
        initUI();
//        UpdateVersion updateVersion = new UpdateVersion(this);
//        updateVersion.startUpdate();
        ApplicationContext.getInstance().addActivity(MenuAct.this);
    }

    private void initUI() {
        //lr
        lyceju = (LinearLayout) findViewById(R.id.ly_ceju);
        lywendu = (LinearLayout) findViewById(R.id.ly_wendu);
        lygps = (LinearLayout) findViewById(R.id.ly_gps);
        lyupdata = (LinearLayout) findViewById(R.id.ly_updata);
        lyupdata.setOnClickListener(this);
        lygps.setOnClickListener(this);
        lywendu.setOnClickListener(this);
        lyceju.setOnClickListener(this);
        //px 和 idx
        layoutid = (LinearLayout) findViewById(R.id.ly_id);
        layoutprint = (LinearLayout) findViewById(R.id.ly_print);
        layoutpasm = (LinearLayout) findViewById(R.id.ly_pasm);
        layoutfinger = (LinearLayout) findViewById(R.id.ly_finger);
        tvversion = (TextView) findViewById(R.id.tv_menu_version);
        layoutfinger.setOnClickListener(this);
        layoutid.setOnClickListener(this);
        layoutpasm.setOnClickListener(this);
        layoutprint.setOnClickListener(this);
        //gx  和  urx
        lyUhf = (LinearLayout) findViewById(R.id.ly_uhf);
        lyUhf.setOnClickListener(this);
        try {
            tvversion.setText("Version_New:" + getVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }
        lyinfrared = (LinearLayout) findViewById(R.id.ly_infrared);
        lyinfrared.setOnClickListener(this);
        progersssDialog = new ProgersssDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showMenu();


    }

    public void showMenu() {
        switch (getEM55Model()) {
            case "1"://em55_px 主要功能为二代证读取 打印机 pasm卡 指纹
                lyceju.setEnabled(false);
                lygps.setEnabled(false);
                lywendu.setEnabled(false);
                layoutpasm.setEnabled(true);
                layoutid.setEnabled(true);
                layoutprint.setEnabled(true);
                layoutfinger.setEnabled(true);
                lyUhf.setEnabled(false);
                lyinfrared.setEnabled(false);
                break;
            case "16"://此背夹为em55_lr 功能为温湿度检测，激光测距，gps，北斗
                layoutpasm.setEnabled(false);
                layoutid.setEnabled(false);
                layoutprint.setEnabled(false);
                layoutfinger.setEnabled(false);
                lyUhf.setEnabled(false);
                lyceju.setEnabled(true);
                lygps.setEnabled(true);
                lywendu.setEnabled(true);
                lyinfrared.setEnabled(false);
                break;
            case "32"://em55_IDX  功能：id2 ，指纹（国内或国外）
                lyceju.setEnabled(false);
                lygps.setEnabled(false);
                lywendu.setEnabled(false);
                layoutpasm.setEnabled(false);
                lyUhf.setEnabled(false);
                layoutid.setEnabled(true);
                layoutprint.setEnabled(false);
                layoutfinger.setEnabled(true);
                lyinfrared.setEnabled(false);
                break;
            case "48"://em55_GX  功能：uhf超高屏，枪柄按键,可以触发主机快捷扫描
                lyceju.setEnabled(false);
                lygps.setEnabled(false);
                lywendu.setEnabled(false);
                layoutpasm.setEnabled(false);
                lyUhf.setEnabled(true);
                layoutid.setEnabled(false);
                layoutprint.setEnabled(false);
                layoutfinger.setEnabled(false);
                lyinfrared.setEnabled(false);
                break;
            case "80"://em55_URX  功能：电容式指纹采集识别，R2000 UHF超高频 ，旗联超高频
                lyceju.setEnabled(false);
                lygps.setEnabled(false);
                lywendu.setEnabled(false);
                layoutpasm.setEnabled(false);
                lyUhf.setEnabled(true);
                layoutid.setEnabled(false);
                layoutprint.setEnabled(false);
                layoutfinger.setEnabled(true);
                lyinfrared.setEnabled(false);
                break;
            case "81"://em55_URX  功能：R2000 UHF超高频 ，旗联超高频/ 红外测温
                lyceju.setEnabled(false);
                lygps.setEnabled(false);
                lywendu.setEnabled(false);
                layoutpasm.setEnabled(false);
                lyUhf.setEnabled(true);
                lyinfrared.setEnabled(true);
                layoutid.setEnabled(false);
                layoutprint.setEnabled(false);
                layoutfinger.setEnabled(false);
                break;
            default:
                lyceju.setEnabled(false);
                lygps.setEnabled(false);
                lywendu.setEnabled(false);
                layoutpasm.setEnabled(false);
                layoutid.setEnabled(false);
                layoutprint.setEnabled(false);
                layoutfinger.setEnabled(false);
                lyUhf.setEnabled(false);
                lyinfrared.setEnabled(false);
                break;
        }
    }

    private DeviceControl deviceControl;
    ProgersssDialog progersssDialog;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_ceju:
                openAct(DistanceAct.class);
                break;
            case R.id.ly_gps:
                openAct(GpsAct.class);
                break;
            case R.id.ly_wendu:
                openAct(TemperatureAct.class);
                break;
            case R.id.ly_updata:
                UpdateVersion updateVersion = new UpdateVersion(this);
                updateVersion.startUpdate();
                break;
        }
        if (v == layoutid) {
            openAct(ID2Act.class);
        } else if (v == layoutprint) {
            openAct(ConnectAvtivity.class);
        } else if (v == layoutpasm) {
            openAct(PsamAct.class);
        } else if (v == layoutfinger) {
//                progersssDialog.show();

            final long start = System.currentTimeMillis();
            showLoading("初始化模块…");
            try {
//                if ("80".equals(getEM55Model()) || "32".equals(getEM55Model())) {
//                    deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN_AND_EXPAND, 63, 5);
//                    deviceControl.PowerOnDevice();
//                } else {
                    deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN_AND_EXPAND, 63, 5,6);
                    deviceControl.PowerOnDevice();
//                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int isFlag = 0;
                    while (isFlag == 0) {
                        if (System.currentTimeMillis() - start > 10000) {
                            isFlag = 4;
                        } else {
                            isFlag = FingerTypes.getrwusbdevices(getApplicationContext());
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initFingerTypes(getrwusbdevices(getApplicationContext()));
                        }
                    });
                }
            }).start();

        } else if (v == lyUhf) {
            openAct(UhfAct.class);
        } else if (v == lyinfrared) {
            openAct(ReadActivity.class);
        }
    }

    public void initFingerTypes(int i) {

        switch (i) {
            case 0:
                showToast("无指纹模块！");
                hideLoading();
                try {
                    deviceControl.PowerOffDevice();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                hideLoading();
                openAct(FpSDKSampleP11MActivity.class);
                break;
            case 2:
                hideLoading();
                openAct(Tcs1Activity.class);
                break;
            case 3:
                hideLoading();
                openAct(UareUSampleJava.class);
                break;
        }

    }

    private FlippingLoadingDialog mProgressDialog;

    public void showLoading(String text) {
        if (mProgressDialog == null) {
            mProgressDialog = new FlippingLoadingDialog(this, text);
        }
        mProgressDialog.setText(text);
        mProgressDialog.show();
    }

    public void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
