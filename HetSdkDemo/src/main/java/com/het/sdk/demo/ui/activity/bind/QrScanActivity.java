package com.het.sdk.demo.ui.activity.bind;

import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.het.basic.base.RxManage;
import com.het.basic.utils.AppTools;
import com.het.basic.utils.GsonUtil;
import com.het.basic.utils.ToastUtil;
import com.het.bind.logic.bean.device.DeviceProductBean;
import com.het.open.lib.api.HetDeviceShareApi;
import com.het.open.lib.api.HetQrCodeApi;
import com.het.open.lib.callback.IHetCallback;
import com.het.sdk.demo.R;
import com.het.sdk.demo.base.BaseHetActivity;
import com.het.sdk.demo.event.HetShareEvent;
import com.het.sdk.demo.model.HetProductModel;
import com.het.sdk.demo.utils.UIJsonConfig;

import java.lang.reflect.Type;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

import static com.het.sdk.demo.ui.activity.bind.WifiBindActivity.VALUE_KEY;


public class QrScanActivity extends BaseHetActivity implements QRCodeView.Delegate {


    private QRCodeView mQRCodeView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_qr_scan;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView(Bundle savedInstanceState) {

        mQRCodeView = (ZXingView) findViewById(R.id.zbarview);
        mQRCodeView.setDelegate(this);
    }

    @Override
    protected void initTopBarView() {
        mTitleView.setTitle(R.string.btn_scan);
        mTitleView.setBackground(UIJsonConfig.getInstance(mContext).setNavBackground_color());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        vibrate();
        this.mQRCodeView.stopSpotAndHiddenRect();
        this.mQRCodeView.stopCamera();
        //shareCode=
        String str = result.substring(0, 9);

        if ("shareCode".equals(str)) {
            String shareCode = result.substring(10, result.length());
            parseQrCodeVer2(shareCode);
        } else if (result.contains("http")) {
            RxManage.getInstance().post("Qr_device_url", result);
            finish();
        } else {
            parseQrCodeVersion(result);
        }
    }

    /**
     * @param code 设备分享码
     */
    private void parseQrCodeVer2(String code) {
        HetDeviceShareApi.getInstance().authShareDevice(new IHetCallback() {
            @Override
            public void onSuccess(int code, String msg) {
                ToastUtil.showToast(mContext, "设备分享成功");
                RxManage.getInstance().post(HetShareEvent.HET_EVENT_MAIN_SHARE_SUCCEE, null);
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtil.showToast(mContext, msg);
                finish();
            }
        }, code, "5");

    }


    //二维码新规则
    private void parseQrCodeVersion(String url) {
        String param = Uri.parse(url).getQueryParameter("param");
        if (TextUtils.isEmpty(param)) {
            tips(getResources().getString(R.string.qr_code_error));
        } else {
            HetQrCodeApi.getInstance().dealQrCode(new IHetCallback() {
                @Override
                public void onSuccess(int code, String msg) {

                    Type type = new TypeToken<HetProductModel>() {
                    }.getType();
                    HetProductModel productBean = GsonUtil.getInstance().toObject(msg, type);

                    DeviceProductBean deviceProductBean = new DeviceProductBean();
                    deviceProductBean.setDeviceTypeId(productBean.getDeviceTypeId());
                    deviceProductBean.setDeviceSubtypeId(productBean.getDeviceSubtypeId());
                    deviceProductBean.setProductId(productBean.getProductId());
                    deviceProductBean.setBindType(productBean.getModuleType());//模块类型（1-WiFi，2-蓝牙，9-ap模式）
                    deviceProductBean.setModuleId(productBean.getModuleId());
                    deviceProductBean.setProductName(productBean.getProductName());
                    deviceProductBean.setRadioCastName(productBean.getRadiocastName());
                    deviceProductBean.setBindType(productBean.getModuleType());
                    Bundle bund = new Bundle();
                    bund.putSerializable(VALUE_KEY, deviceProductBean);

                    if (deviceProductBean.getBindType() == 1 || deviceProductBean.getBindType() == 9) {
                        AppTools.startForwardActivity(QrScanActivity.this, WifiBindActivity.class, bund, Boolean.valueOf(true));
                    } else if (deviceProductBean.getBindType() == 2) {
                        AppTools.startForwardActivity(QrScanActivity.this, BleBindActivity.class, bund, Boolean.valueOf(true));
                    }
                }

                @Override
                public void onFailed(int code, String msg) {
                    tips(msg);
                    finish();
                }
            }, url);
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Toast.makeText(this, "打开相机出错", Toast.LENGTH_SHORT).show();
        finish();
    }
}