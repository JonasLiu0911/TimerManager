package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.databinding.ActivityGenderChangeBinding;
import cn.xtu.lhj.timermanager.databinding.ActivityNameChangeBinding;

public class GenderChangeActivity extends BaseActivity {

    private final String TAG = "GenderChangeActivity";

    ActionBar actionBar;
    Button submitBtn;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActivityGenderChangeBinding genderChangeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_gender_change);
        genderChangeBinding = DataBindingUtil.setContentView(this, R.layout.activity_gender_change);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("修改性别");
        }

        submitBtn = findViewById(R.id.bt_submit_gender_changed);
        OnClickSubmit onClickSubmit = new OnClickSubmit();
        submitBtn.setOnClickListener(onClickSubmit);
    }

    // 按钮监听
    private class OnClickSubmit implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
            String telephone = sharedPreferences.getString("telephone", "");

            String gender = genderChangeBinding.newGender.getText().toString();

            if (TextUtils.isEmpty(gender)) {
                Toast.makeText(GenderChangeActivity.this, "性别不能为空", Toast.LENGTH_SHORT).show();
            } else {
                asyncUpdateGenderWithXHttp2(telephone, gender);
            }
        }
    }

    /**
     * XHttp2请求后端接口 更新用户性别
     * @param telephone
     * @param gender
     */
    private void asyncUpdateGenderWithXHttp2(final String telephone, final String gender) {

        if (gender == null) {
            Toast.makeText(GenderChangeActivity.this, "性别不能为空，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }

        XHttp.post(NetConstant.getUpdateGenderURL())
                .params("telephone", telephone)
                .params("gender", gender)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {

                    @Override
                    public void onSuccess(Object response) throws Throwable {

                        editor = sharedPreferences.edit();
                        editor.putString("gender", gender);

                        if (editor.commit()) {
                            Toast.makeText(GenderChangeActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            showToastInThread(GenderChangeActivity.this, "未保存，修改失败");
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Toast.makeText(GenderChangeActivity.this, "ttttttt", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(GenderChangeActivity.this, e.getMessage());
                    }
                });
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
}