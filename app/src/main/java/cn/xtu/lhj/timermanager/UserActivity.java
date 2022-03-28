package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.xtu.lhj.timermanager.constant.ModelConstant;

public class UserActivity extends BaseActivity {

    ActionBar actionBar;
    Button logoutBtn;
    RelativeLayout toChangeInfoPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenConfig();
        setContentView(R.layout.activity_user);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("个人中心");
        }

        // 跳转个人信息修改页
        toChangeInfoPage = findViewById(R.id.re_change_info);
        OnClickToChange onClickToChange = new OnClickToChange();
        toChangeInfoPage.setOnClickListener(onClickToChange);

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
            case R.id.exit:
                Intent intent = new Intent(UserActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.update:
                Toast.makeText(UserActivity.this, "用户信息更新", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    // 实例化菜单并显示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_title_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    // 跳转个人信息修改监听
    private class OnClickToChange implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(UserActivity.this, InfoChangeActivity.class);
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

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences sharedPreferences = getSharedPreferences(ModelConstant.LOGIN_INFO, MODE_PRIVATE);
                    sharedPreferences.edit().clear().apply();
                    // 跳转到登录页
                    Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }
}