package cn.xtu.lhj.timermanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.filament.View;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import cn.xtu.lhj.timermanager.constant.ModelConstant;
import cn.xtu.lhj.timermanager.constant.NetConstant;

public class CountDownActivity extends BaseActivity {

    private CountDownTimer countDownTimer;

    final static String TAG = "CountDownActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setStatusBarColor(Color.TRANSPARENT);       //将状态栏设置成透明色
        }

        fullScreenConfig();

        // 设置布局
        setContentView(R.layout.activity_count_down);

        autoLogin();

        initCountDown();
    }

    private void autoLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences(ModelConstant.LOGIN_INFO, MODE_PRIVATE);
        String telephoneInSp = sharedPreferences.getString("telephone", "");
        String passwordInSp = sharedPreferences.getString("encryptedPassword", "");

        // 异步登录
        asyncValidateWithXHttp2(telephoneInSp, passwordInSp);
    }

    private void asyncValidateWithXHttp2(String telephone, String password) {
        XHttp.post(NetConstant.getLoginURL())
                .params("telephone", telephone)
                .params("password", password)
                .params("type", "autoLogin")
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object data) throws Throwable {
                        Log.d(TAG, "请求Url成功，自动登录成功");
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常，自动登录失败" + e.toString());
                    }
                });
    }

    private void initCountDown() {
        countDownTimer = new CountDownTimer(1000 * 3, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                jumpTo();
            }
        }.start();
    }

    private void jumpTo() {

        Intent intent = new Intent(CountDownActivity.this, MainActivity.class);
        startActivity(intent);

        destroyTimer();
        finish();

    }

    public void destroyTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}