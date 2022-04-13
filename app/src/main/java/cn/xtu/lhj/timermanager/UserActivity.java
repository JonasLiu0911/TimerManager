package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import cn.xtu.lhj.timermanager.bean.UserInfo;
import cn.xtu.lhj.timermanager.constant.ModelConstant;
import cn.xtu.lhj.timermanager.constant.NetConstant;

public class UserActivity extends BaseActivity {

    private final String TAG = "UserActivity";

    ActionBar actionBar;
    Button logoutBtn;
    RelativeLayout toChangeInfoPage;
    RelativeLayout toHistoryPage;
    TextView nameToFill;
    TextView telephoneToFill;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenConfig();
        setContentView(R.layout.activity_user);

        initPage();

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("个人中心");
        }

        // 跳转个人信息修改页
        toChangeInfoPage = findViewById(R.id.re_my_info);
        OnClickToChange onClickToChange = new OnClickToChange();
        toChangeInfoPage.setOnClickListener(onClickToChange);

        toHistoryPage = findViewById(R.id.re_history_trip);
        OnClickToHistory onClickToHistory = new OnClickToHistory();
        toHistoryPage.setOnClickListener(onClickToHistory);


        // 退出登录按钮
        logoutBtn = findViewById(R.id.bt_logout);
        OnClickLogout onClickLogout = new OnClickLogout();
        logoutBtn.setOnClickListener(onClickLogout);

    }

    // 菜单点击实现
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }


    public void initPage() {
        nameToFill = findViewById(R.id.user_name_to_fill);
        telephoneToFill = findViewById(R.id.user_telephone_to_fill);
        SharedPreferences sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
        asyncGetUserInfoWithXHttp2(sharedPreferences.getString("telephone", ""));
        telephoneToFill.setText(sharedPreferences.getString("telephone", ""));
        Log.d("telephoneFilled", sharedPreferences.getString("telephone", ""));
    }


    // 跳转个人信息修改监听
    private class OnClickToChange implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(UserActivity.this, InfoChangeActivity.class);
            startActivity(intent);
        }
    }

    private class OnClickToHistory implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(UserActivity.this, HistoryActivity.class);
            startActivity(intent);
        }
    }

    // 退出登录按钮监听
    private class OnClickLogout implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
            builder.setTitle("提示");
            builder.setMessage("是否确定退出");
            builder.setIcon(R.drawable.warning);

            builder.setPositiveButton("确认", (dialog, which) -> {
                SharedPreferences sharedPreferences = getSharedPreferences(ModelConstant.LOGIN_INFO, MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();
                // 跳转到登录页
                Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
            builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            builder.create().show();

        }
    }

    // 请求用户信息
    private void asyncGetUserInfoWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetUserInfoURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<UserInfo>() {
                    @Override
                    public void onSuccess(UserInfo data) {
                        Log.d(TAG, "请求URL成功：" + data);
                        if (data != null) {
                            String name = data.getName();
                            nameToFill.setText(name);
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(UserActivity.this, e.getMessage());
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        initPage();
    }
}