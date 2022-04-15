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

import com.bumptech.glide.Glide;
import com.google.android.filament.View;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import cn.xtu.lhj.timermanager.bean.UserInfo;
import cn.xtu.lhj.timermanager.constant.ModelConstant;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.utils.SPUtils;

public class CountDownActivity extends BaseActivity {

    private CountDownTimer countDownTimer;

    final static String TAG = "CountDownActivity";

    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;

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

        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

        autoLogin();

        asyncGetUserInfoWithXHttp2(sharedPreferences.getString("telephone", ""));

        initCountDown();
    }

    // 异步 自动登录
    public void autoLogin() {

        String telToAutoLogin = sharedPreferences.getString("telephone", "");
        String pwdInToAutoLogin = sharedPreferences.getString("encryptedPassword", "");

        asyncValidateWithXHttp2(telToAutoLogin, pwdInToAutoLogin);
    }

    public void asyncValidateWithXHttp2(String telephone, String password) {
        XHttp.post(NetConstant.getLoginURL())
                .params("telephone", telephone)
                .params("password", password)
                .params("type", "autoLogin")
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.d(TAG, "请求Url成功，自动登录成功");
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常，自动登录失败" + e.toString());
                    }
                });
    }

    private void asyncGetUserInfoWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetUserInfoURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<UserInfo>() {
                    @Override
                    public void onSuccess(UserInfo data) {
                        Log.d(TAG, "CountDown 请求URL成功：" + data);
                        Log.d(TAG, "name: " + data.getName());
                        Log.d(TAG, "age: " + data.getAge());
                        Log.d(TAG, "gender: " + data.getGender());
                        Log.d(TAG, "head: " + data.getHeadUrl());

                        editor = sharedPreferences.edit();

                        editor.putString("nameBegin", data.getName());
                        editor.putString("age", data.getAge().toString());
                        editor.putString("gender", data.getGender() == 1 ? "男" : "女");
                        editor.putString("imageUrl", data.getHeadUrl());
                        editor.commit();

                        Log.d(TAG, "head: " + sharedPreferences.getString("imageUrl", "0"));
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(CountDownActivity.this, e.getMessage());
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