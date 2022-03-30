package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

public class InfoChangeActivity extends BaseActivity {

    ActionBar actionBar;
    ImageView headToFill;
    TextView nickNameToFill;
    TextView genderToFill;
    TextView ageToFill;

    RelativeLayout toHeadChange;
    RelativeLayout toNameChange;
    RelativeLayout toGenderChange;
    RelativeLayout toAgeChange;
    RelativeLayout toPwdChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_change);

        initPage();

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("个人资料");
        }
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
        headToFill = findViewById(R.id.change_head);
        nickNameToFill = findViewById(R.id.change_et_nickname);
        genderToFill = findViewById(R.id.change_et_gender);
        ageToFill = findViewById(R.id.change_et_age);

        headToFill.setImageResource(R.drawable.background);
        nickNameToFill.setText("haha");
        genderToFill.setText("女");
        ageToFill.setText("24");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.re_head_in_change:
                toHeadChange = findViewById(R.id.re_head_in_change);
                toHeadChange.setOnClickListener(new OnClickChangeHead());
                break;
            case R.id.re_nickname_in_change:
                toNameChange = findViewById(R.id.re_nickname_in_change);
                toNameChange.setOnClickListener(new OnClickChangeName());
                break;
            case R.id.re_gender_in_change:
                toGenderChange = findViewById(R.id.re_gender_in_change);
                toGenderChange.setOnClickListener(new OnClickChangeGender());
                break;
            case R.id.re_age_in_change:
                toAgeChange = findViewById(R.id.re_age_in_change);
                toAgeChange.setOnClickListener(new OnClickChangeAge());
                break;
            case R.id.re_pwd_in_change:
                toPwdChange = findViewById(R.id.re_pwd_in_change);
                toPwdChange.setOnClickListener(new OnClickChangePwd());
                break;
            default:
                break;
        }
    }

    // 修改头像 监听
    private class OnClickChangeHead implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(InfoChangeActivity.this, "修改头像", Toast.LENGTH_SHORT).show();
        }
    }

    // 跳转修改昵称 监听
    private class OnClickChangeName implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(InfoChangeActivity.this, NameChangeActivity.class);
            startActivity(intent);
        }
    }

    // 跳转修改性别 监听
    private class OnClickChangeGender implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(InfoChangeActivity.this, GenderChangeActivity.class);
            startActivity(intent);
        }
    }

    // 跳转修改年龄 监听
    private class OnClickChangeAge implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(InfoChangeActivity.this, AgeChangeActivity.class);
            startActivity(intent);
        }
    }

    // 跳转修改密码 监听
    private class OnClickChangePwd implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(InfoChangeActivity.this, PwdChangeActivity.class);
            startActivity(intent);
        }
    }
}