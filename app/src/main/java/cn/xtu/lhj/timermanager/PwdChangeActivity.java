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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import cn.xtu.lhj.timermanager.constant.ModelConstant;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.databinding.ActivityPwdChangeBinding;
import cn.xtu.lhj.timermanager.utils.ValidUtils;

public class PwdChangeActivity extends BaseActivity {

    private final String TAG = "PwdChangeActivity";

    ActionBar actionBar;
    RelativeLayout oldPwd;
    RelativeLayout newPwd;
    RelativeLayout newPwdAgain;
    Button submitBtn1;
    Button submitBtn2;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActivityPwdChangeBinding pwdChangeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pwdChangeBinding = DataBindingUtil.setContentView(this, R.layout.activity_pwd_change);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("修改密码");
        }

        // 控件获取
        oldPwd = findViewById(R.id.re_old_pwd);
        newPwd = findViewById(R.id.re_pwd);
        newPwdAgain = findViewById(R.id.re_pwd_again);
        submitBtn1 = findViewById(R.id.bt_submit_pwd_old);
        submitBtn2 = findViewById(R.id.bt_submit_pwd_changed);
        // 控件初始状态设置
        newPwd.setVisibility(View.INVISIBLE);
        newPwdAgain.setVisibility(View.INVISIBLE);
        submitBtn2.setVisibility(View.INVISIBLE);

        // 监听事件
        OnClickSubmitOld onClickSubmitOld = new OnClickSubmitOld();
        submitBtn1.setOnClickListener(onClickSubmitOld);

        OnClickSubmitPwd onClickSubmitPwd = new OnClickSubmitPwd();
        submitBtn2.setOnClickListener(onClickSubmitPwd);

        setOnFocusChangeErrMsg();

    }

    private void setOnFocusChangeErrMsg() {
        pwdChangeBinding.etNewPwd.setOnFocusChangeListener(
                (v, hasFocus) -> {
                    String inputStr = pwdChangeBinding.etNewPwd.getText().toString();
                    if (!hasFocus) {
                        if (!ValidUtils.isPasswordValid(inputStr)) {
                            pwdChangeBinding.etNewPwd.setError("密码必须不少于6位");
                        }
                    }
                }
        );
    }

    // 按钮1监听
    private class OnClickSubmitOld implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            // 判断原密码是否正确
            String inputStr = pwdChangeBinding.etOldPwd.getText().toString();
            if (TextUtils.isEmpty(inputStr)) {
                Toast.makeText(PwdChangeActivity.this, "输入为空", Toast.LENGTH_SHORT).show();
                pwdChangeBinding.etOldPwd.clearFocus();
                return;
            }
            String encryptedInputStr = null;
            try {
                encryptedInputStr = ValidUtils.encodeByMD5(inputStr);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
            String encryptedPassword = sharedPreferences.getString("encryptedPassword", "");

            if (encryptedInputStr.equals(encryptedPassword)) {
                submitBtn1.setVisibility(View.INVISIBLE);
                oldPwd.setVisibility(View.INVISIBLE);
                newPwd.setVisibility(View.VISIBLE);
                newPwdAgain.setVisibility(View.VISIBLE);
                submitBtn2.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(PwdChangeActivity.this, "密码有误，请重新输入", Toast.LENGTH_SHORT).show();
                pwdChangeBinding.etOldPwd.setText("");
                pwdChangeBinding.etOldPwd.clearFocus();
                return;
            }

        }
    }

    // 按钮2监听
    private class OnClickSubmitPwd implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
            String telephone = sharedPreferences.getString("telephone", "");
            String password1 = pwdChangeBinding.etNewPwd.getText().toString();
            String password2 = pwdChangeBinding.etNewPwdAgain.getText().toString();

            asyncUpdatePwdWithXHttp2(telephone, password1, password2);
        }
    }

    /**
     * XHttp2请求后端接口 更新用户密码
     * @param telephone
     * @param password1
     * @param password2
     */
    private void asyncUpdatePwdWithXHttp2(final String telephone, final String password1, final String password2) {

        // 判空校验
        if (TextUtils.isEmpty(telephone) || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2)) {
            Toast.makeText(PwdChangeActivity.this, "密码不能为空，请输入", Toast.LENGTH_SHORT).show();
            return;
        }

        // 密码一致校验
        if (!TextUtils.equals(password1, password2)) {
            Toast.makeText(PwdChangeActivity.this, "两次密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }

        // 通过校验，请求修改密码
        XHttp.post(NetConstant.getUpdatePwdURL())
                .params("telephone", telephone)
                .params("password", password1)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object data) throws Throwable {
                        sharedPreferences = getSharedPreferences(ModelConstant.LOGIN_INFO, MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putString("telephone", telephone);
                        String encryptedPassword = ValidUtils.encodeByMD5(password1);
                        editor.putString("encryptedPassword", encryptedPassword);

                        if (editor.commit()) {
                            Toast.makeText(PwdChangeActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            showToastInThread(PwdChangeActivity.this, "未保存，修改失败");
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
//                        showToastInThread(PwdChangeActivity.this, e.getMessage());
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