package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

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
import cn.xtu.lhj.timermanager.databinding.ActivityAgeChangeBinding;
import cn.xtu.lhj.timermanager.databinding.ActivityNameChangeBinding;

public class AgeChangeActivity extends BaseActivity {

    private final String TAG = "AgeChangeActivity";

    ActionBar actionBar;
    Button submitBtn;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActivityAgeChangeBinding ageChangeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ageChangeBinding = DataBindingUtil.setContentView(this, R.layout.activity_age_change);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("修改年龄");
        }

        submitBtn = findViewById(R.id.bt_submit_age_changed);
        OnClickSubmitAge onClickSubmitAge = new OnClickSubmitAge();
        submitBtn.setOnClickListener(onClickSubmitAge);
    }

    // 按钮监听
    private class OnClickSubmitAge implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
            String telephone = sharedPreferences.getString("telephone", "");
            String age = ageChangeBinding.newAge.getText().toString();

            if (TextUtils.isEmpty(age)) {
                Toast.makeText(AgeChangeActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
            } else {
                asyncUpdateAgeWithXHttp2(telephone, age);
            }
        }
    }

    /**
     * XHttp2请求后端接口 更新用户年龄
     * @param telephone
     * @param age
     */
    private void asyncUpdateAgeWithXHttp2(final String telephone, final String age) {
        XHttp.post(NetConstant.getUpdateAgeURL())
                .params("telephone", telephone)
                .params("age", age)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) {
                        editor = sharedPreferences.edit();
                        editor.putString("age", age);

                        if (editor.commit()) {
                            Toast.makeText(AgeChangeActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            showToastInThread(AgeChangeActivity.this, "未保存，修改失败");
                        }
                    }


                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(AgeChangeActivity.this, e.getMessage());
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