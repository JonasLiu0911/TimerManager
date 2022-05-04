package cn.xtu.lhj.timermanager;

import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import cn.xtu.lhj.timermanager.bean.OtpCode;
import cn.xtu.lhj.timermanager.constant.ModelConstant;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.databinding.ActivityRegisterBinding;
import cn.xtu.lhj.timermanager.utils.ValidUtils;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG = "RegisterActivity";

    String account = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActivityRegisterBinding registerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenConfig();
        getSupportActionBar().hide();

        registerBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        Intent intent = getIntent();
        account = intent.getStringExtra("account");
        registerBinding.etTelephone.setText(account);

        setOnClickListener();

        setOnFocusChangeErrMsg(registerBinding.etTelephone, "phone", "手机号码格式不正确");
        setOnFocusChangeErrMsg(registerBinding.etPassword, "password", "密码必须不少于6位");
        setOnFocusChangeErrMsg(registerBinding.etGender, "gender", "性别只能填1或2");
    }

    // 为点击事件UI对象设置监听器
    private void setOnClickListener() {
        registerBinding.btGetOtp.setOnClickListener(this);
        registerBinding.btSubmitRegister.setOnClickListener(this);
    }

    private void setOnFocusChangeErrMsg(EditText editText, String inputType, String errMsg) {
        editText.setOnFocusChangeListener(
                (v, hasFocus) -> {
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

                            case "gender":
                                if (!ValidUtils.isGenderValid(inputStr)) {
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

    public void onClick(View view) {

        String telephone = registerBinding.etTelephone.getText().toString();
        String otpCode = registerBinding.etOtpCode.getText().toString();
        String username = registerBinding.etUsername.getText().toString();
        String gender = registerBinding.etGender.getText().toString();
        String age = registerBinding.etAge.getText().toString();
        String password1 = registerBinding.etPassword.getText().toString();
        String password2 = registerBinding.etPassword2.getText().toString();

        switch (view.getId()) {
            case R.id.bt_get_otp:
                if (TextUtils.isEmpty(telephone)) {
                    Toast.makeText(RegisterActivity.this, "手机号码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (ValidUtils.isPhoneValid(telephone)) {
                        asyncGetOtpCodeWithXHttp2(telephone);
                    } else {
                        Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.bt_submit_register:
                asyncRegisterWithXHttp2(telephone, otpCode, username, gender, age, password1, password2);
                break;
        }
    }

    private void asyncGetOtpCodeWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetOtpCodeURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<OtpCode>() {
                    @Override
                    public void onSuccess(OtpCode data) {
                        Log.d(TAG, "请求Url成功：" + data);
                        if (data != null) {
                            String otpCode = data.getOtpCode();

                            //自动填充验证码（不要）
//                            setTextInThread(registerBinding.etOtpCode, otpCode);

                            //子线程中显示toast
                            showToastInThread(RegisterActivity.this, "您的验证码：" + otpCode + "，请查收");
                            Log.d(TAG, "telephone: " + telephone + " otpCode: " + otpCode);
                        }
                        Log.d(TAG, "验证码已发送， 请注意查收");
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(RegisterActivity.this, e.getMessage());
                    }
                });
    }

    // 用户请求验证码后更新UI，将验证码直接写在页面上
    private void setTextInThread(EditText editText, String otpCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editText.setText(otpCode);
            }
        });
    }


    /**
     * XHttp2请求后端接口 注册
     * @param telephone
     * @param otpCode
     * @param username
     * @param gender
     * @param age
     * @param password1
     * @param password2
     */
    private void asyncRegisterWithXHttp2(
            final String telephone,
            final String otpCode,
            final String username,
            final String gender,
            final String age,
            final String password1,
            final String password2) {
        // 判空校验
        if (TextUtils.isEmpty(telephone) || TextUtils.isEmpty(otpCode)
                || TextUtils.isEmpty(username) || TextUtils.isEmpty(gender)
                || TextUtils.isEmpty(age) || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2)) {
            Toast.makeText(RegisterActivity.this, "存在输入为空，请重新注册", Toast.LENGTH_SHORT).show();
            return;
        }

        // 密码一致校验
        if (!TextUtils.equals(password1, password2)) {
            Toast.makeText(RegisterActivity.this, "两次密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }

        // 通过校验，注册
        XHttp.post(NetConstant.getRegisterURL())
                .params("telephone", telephone)
                .params("otpCode", otpCode)
                .params("name", username)
                .params("gender", gender)
                .params("age", age)
                .params("password", password1)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object data) throws Throwable {
                        sharedPreferences = getSharedPreferences(ModelConstant.LOGIN_INFO, MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putString("telephone", telephone);
                        editor.putString("name", username);
                        String encryptedPassword = ValidUtils.encodeByMD5(password1);
                        editor.putString("encryptedPassword", encryptedPassword);

                        if (editor.commit()) {
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showToastInThread(RegisterActivity.this, "注册失败");
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
//                        showToastInThread(RegisterActivity.this, e.getMessage());
                    }
                });
    }
}