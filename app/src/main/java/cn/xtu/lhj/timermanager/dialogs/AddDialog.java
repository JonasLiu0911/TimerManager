package cn.xtu.lhj.timermanager.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import cn.xtu.lhj.timermanager.R;

public class AddDialog extends AlertDialog {

    private OnClickListener onClickListener;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public EditText edtTitle;
    public EditText edtDesc;
    public Button btPickTime;
    public Button btPickAddress;

    public Button submit;
    public Button cancel;


    public AddDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_dialog);

        setCanceledOnTouchOutside(false);

        setDialogPosition();

        initView();

        initEvent();
    }

    private void setDialogPosition() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = getWindow();
        layoutParams.copyFrom(window.getAttributes());

        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;

        window.setAttributes(layoutParams);
    }

    private void initView() {
        edtTitle = findViewById(R.id.edt_title);
        edtDesc = findViewById(R.id.edt_desc);
        btPickTime = findViewById(R.id.btn_pick_time);
        btPickAddress = findViewById(R.id.btn_pick_address);
        submit = findViewById(R.id.btn_submit);
        cancel = findViewById(R.id.btn_cancel);
    }

    private void initEvent() {
        OnClickPickTime clickPickTime = new OnClickPickTime();
        OnClickPickAddress clickPickAddress = new OnClickPickAddress();
        OnClickSubmit clickSubmit = new OnClickSubmit();
        OnClickCancel clickCancel = new OnClickCancel();


        btPickTime.setOnClickListener(clickPickTime);
        btPickAddress.setOnClickListener(clickPickAddress);
        submit.setOnClickListener(clickSubmit);
        cancel.setOnClickListener(clickCancel);

    }

    private class OnClickPickTime implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onPickTimeClick();
            }
        }
    }
    private class OnClickPickAddress implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onPickAddressClick();
            }
        }
    }
    private class OnClickSubmit implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onSubmitClick();
            }
            dismiss();
        }
    }
    private class OnClickCancel implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onCancelClick();
            }
            dismiss();
        }
    }

    // 提供给外部使用的方法
    public AddDialog setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
        return this;
    }

    public interface OnClickListener {
        void onPickTimeClick();
        void onPickAddressClick();
        void onSubmitClick();
        void onCancelClick();
    }
}
