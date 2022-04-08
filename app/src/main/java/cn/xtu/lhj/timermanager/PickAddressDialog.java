package cn.xtu.lhj.timermanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.baidu.mapapi.map.MapView;

public class PickAddressDialog extends AlertDialog {

    private OnClickListener onClickListener;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public EditText edtInputAddress;
    public ImageView ivToSearch;
    public MapView mvInDialog;

    public Button submitAddress;
    public Button cancelAddress;


    protected PickAddressDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_address_dialog);

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
        edtInputAddress = findViewById(R.id.et_input_poi);
        ivToSearch = findViewById(R.id.iv_to_search);
        mvInDialog = findViewById(R.id.map_view_in_dialog);
        submitAddress = findViewById(R.id.btn_submit_address);
        cancelAddress = findViewById(R.id.btn_cancel_address);
    }

    private void initEvent() {

        OnClickToSearch clickToSearch = new OnClickToSearch();
        OnClickCancelAddress clickCancelAddress = new OnClickCancelAddress();
        OnClickSubmitAddress clickSubmitAddress = new OnClickSubmitAddress();

        ivToSearch.setOnClickListener(clickToSearch);
        submitAddress.setOnClickListener(clickSubmitAddress);
        cancelAddress.setOnClickListener(clickCancelAddress);
    }

    private class OnClickToSearch implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onToSearchClick();
            }
        }
    }

    private class OnClickCancelAddress implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onCancelAddressClick();
            }
            dismiss();
        }
    }

    private class OnClickSubmitAddress implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onSubmitAddressClick();
            }
            dismiss();
        }
    }

    // 提供给外部使用的方法
    public PickAddressDialog setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
        return this;
    }

    public interface OnClickListener {
        void onToSearchClick();
        void onCancelAddressClick();
        void onSubmitAddressClick();
    }

}
