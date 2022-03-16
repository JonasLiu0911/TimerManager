package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import cn.xtu.lhj.timermanager.constant.ModelConstant;

public class UserActivity extends BaseActivity {

    Button backBtn;
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenConfig();
        setContentView(R.layout.activity_user);


        // 标题栏 返回键
        backBtn = findViewById(R.id.title_back);
        OnClickBack onClickBack = new OnClickBack();
        backBtn.setOnClickListener(onClickBack);

        // 退出登录按钮
        logoutBtn = findViewById(R.id.bt_logout);
        OnClickLogout onClickLogout = new OnClickLogout();
        logoutBtn.setOnClickListener(onClickLogout);

    }

    // 标题栏返回键监听
    private class OnClickBack implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            finish();
        }
    }

    // 退出登录按钮监听
    private class OnClickLogout implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            SharedPreferences sharedPreferences = getSharedPreferences(ModelConstant.LOGIN_INFO, MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            // 跳转到登录页
            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
            startActivity(intent);

            // 关闭当前页
            finish();

        }
    }
}