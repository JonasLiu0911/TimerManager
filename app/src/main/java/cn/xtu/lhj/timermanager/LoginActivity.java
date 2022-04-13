package cn.xtu.lhj.timermanager;

import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;


import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.databinding.ActivityLoginBinding;
import cn.xtu.lhj.timermanager.utils.ValidUtils;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    //SharedPreferences对象
    SharedPreferences sharedPreferences;
    //SharedPreferences编辑器对象
    SharedPreferences.Editor editor;

    private final String TAG = "LoginActivity";

    private ActivityLoginBinding loginBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenConfig();
        getSupportActionBar().hide();

        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        setOnClickListener();

        setOnFocusChangeErrMsg(loginBinding.etAccount, "phone", "手机号码格式不正确");
        setOnFocusChangeErrMsg(loginBinding.etPassword, "password", "密码必须不少于6位");

    }

    //给点击事件的UI对象设置监听器
    private void setOnClickListener() {
        loginBinding.btLogin.setOnClickListener(this);
        loginBinding.tvToRegister.setOnClickListener(this);
        loginBinding.tvForgetPassword.setOnClickListener(this);
        loginBinding.tvServiceAgreement.setOnClickListener(this);
        loginBinding.ivThirdMethod1.setOnClickListener(this);
        loginBinding.ivThirdMethod2.setOnClickListener(this);
        loginBinding.ivThirdMethod3.setOnClickListener(this);
    }

    //校验账号、密码的合法性
    private void setOnFocusChangeErrMsg(EditText editText, String inputType, String errMsg) {
        editText.setOnFocusChangeListener(
                (view, hasFocus) -> {
                    String inputStr = editText.getText().toString();
                    if (!hasFocus) {
                        switch (inputType) {
                            case "phone":
                                if (!ValidUtils.isPhoneValid(inputStr)) {
                                    editText.setError(errMsg);
                                }
                                break;
                            case "password":
                                if (!ValidUtils.isPasswordValid(inputStr)) {
                                    editText.setError(errMsg);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
        );
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        String account = loginBinding.etAccount.getText().toString();
        String password = loginBinding.etPassword.getText().toString();

        switch (view.getId()) {
            // 登录按钮响应事件
            case R.id.bt_login:
                loginBinding.etPassword.clearFocus();
                if (!(ValidUtils.isPhoneValid(account) && ValidUtils.isPasswordValid(password))) {
                    Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                    break;
                }
                asyncLoginWithXHttp2(account, password);
                break;

            // 注册按钮响应事件
            case R.id.tv_to_register:
                Intent intent = new Intent(this, RegisterActivity.class);
                intent.putExtra("account", account);
                startActivity(intent);
                break;

            // 以下都暂未实现
            // 忘记密码
            case R.id.tv_forget_password:
                break;

            // 点击查看用户协议
            case R.id.tv_service_agreement:
                break;

            // 跳转第三方方式1
            case R.id.iv_third_method1:
                break;

            // 跳转第三方方式2
            case R.id.iv_third_method2:
                break;

            // 跳转第三方方式3
            case R.id.iv_third_method3:
                break;


        }
    }

    /**
     * XHttp2请求后端接口 登录
     * @param telephone
     * @param password
     */
    private void asyncLoginWithXHttp2(String telephone, String password) {
        XHttp.post(NetConstant.getLoginURL())
                .params("telephone", telephone)
                .params("password", password)
                .params("type", "login")
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {

                    @Override
                    public void onSuccess(Object obj) throws Throwable {
                        Log.d(TAG, "请求Url成功，登录成功");
                        String encryptedPassword = ValidUtils.encodeByMD5(password);
                        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putString("telephone", telephone);
                        editor.putString("encryptedPassword", encryptedPassword);

                        if (editor.commit()) {
                            Intent login_to_main = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(login_to_main);
                            finish();
                        } else {
                            showToastInThread(LoginActivity.this, "验证失败，请重新登录");
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url失败：" + e.getMessage());
                        showToastInThread(LoginActivity.this, e.getMessage());
                    }
                });
    }
}