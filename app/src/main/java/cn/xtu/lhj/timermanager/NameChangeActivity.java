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
import cn.xtu.lhj.timermanager.databinding.ActivityInfoChangeBinding;
import cn.xtu.lhj.timermanager.databinding.ActivityNameChangeBinding;

public class NameChangeActivity extends BaseActivity {

    private final String TAG = "NameChangeActivity";

    ActionBar actionBar;
    Button submitBtn;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActivityNameChangeBinding nameChangeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nameChangeBinding = DataBindingUtil.setContentView(this, R.layout.activity_name_change);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("修改昵称");
        }

        submitBtn = findViewById(R.id.bt_submit_name_changed);
        OnClickSubmit onClickSubmit = new OnClickSubmit();
        submitBtn.setOnClickListener(onClickSubmit);

    }

    // 按钮监听
    private class OnClickSubmit implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
            String telephone = sharedPreferences.getString("telephone", "");
            String name = nameChangeBinding.newNickname.getText().toString();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(NameChangeActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            } else {
                    asyncUpdateNicknameWithXHttp2(telephone, name);
            }
        }
    }

    /**
     * XHttp2请求后端接口 更新用户昵称
     * @param telephone
     * @param name
     */
    private void asyncUpdateNicknameWithXHttp2(final String telephone, final String name) {

        if (name == null) {
            Toast.makeText(NameChangeActivity.this, "昵称不能为空，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }

        XHttp.post(NetConstant.getUpdateNicknameURL())
                .params("telephone", telephone)
                .params("name", name)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {

                    @Override
                    public void onSuccess(Object response) {

                        editor = sharedPreferences.edit();
                        editor.putString("name", name);

                        if (editor.commit()) {
                            Toast.makeText(NameChangeActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            showToastInThread(NameChangeActivity.this, "未保存，修改失败");
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
//                        showToastInThread(NameChangeActivity.this, e.getMessage());
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