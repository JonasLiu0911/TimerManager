package cn.xtu.lhj.timermanager.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.baidu.mapapi.map.MapView;

import cn.xtu.lhj.timermanager.R;

public class DetailDialog extends AlertDialog {

    private OnClickListener onClickListener;

    public EditText detailTitle;
    public EditText detailDesc;
    public TextView detailTimeStr;
    public CardView detailAddressCard;
    public MapView detailAddressBaiduMap;
    public ImageView detailPoint;

    public Button updateCancel;
    public Button updateBegin;
    public Button updateSubmit;

    public DetailDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_detail);

        setCanceledOnTouchOutside(true);

        setDialogPosition();

        initView();

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
        detailTitle = findViewById(R.id.title_detail_et);
        detailDesc = findViewById(R.id.desc_detail_et);
        detailTimeStr = findViewById(R.id.time_detail_tv);
        detailAddressCard = findViewById(R.id.detail_map_card);
        detailAddressBaiduMap = findViewById(R.id.address_detail_map);
        detailPoint = findViewById(R.id.detail_address_point);

        updateBegin = findViewById(R.id.update_begin_btn);
        updateCancel = findViewById(R.id.btn_cancel_update);
        updateSubmit = findViewById(R.id.btn_submit_update);

        detailTitle.setFocusableInTouchMode(false);
        detailDesc.setFocusableInTouchMode(false);
        updateCancel.setVisibility(View.INVISIBLE);
        updateSubmit.setVisibility(View.INVISIBLE);
        detailPoint.setVisibility(View.INVISIBLE);

        OnClickBeginUpdate clickBeginUpdate = new OnClickBeginUpdate();
        updateBegin.setOnClickListener(clickBeginUpdate);
    }

    public void initEvent() {
        OnClickUpdateTime clickUpdateTime = new OnClickUpdateTime();
        OnClickUpdateAddress clickUpdateAddress = new OnClickUpdateAddress();

        OnClickCancelUpdate clickCancelUpdate = new OnClickCancelUpdate();
        OnClickSubmitUpdate clickSubmitUpdate = new OnClickSubmitUpdate();

        updateCancel.setVisibility(View.VISIBLE);
        updateSubmit.setVisibility(View.VISIBLE);
        updateBegin.setVisibility(View.INVISIBLE);
        detailTitle.setFocusableInTouchMode(true);
        detailDesc.setFocusableInTouchMode(true);

        detailTimeStr.setOnClickListener(clickUpdateTime);
        detailPoint.setOnClickListener(clickUpdateAddress);
        updateCancel.setOnClickListener(clickCancelUpdate);
        updateSubmit.setOnClickListener(clickSubmitUpdate);
    }

    private class OnClickUpdateTime implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.updateTimeClick();
            }
        }
    }

    private class OnClickUpdateAddress implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.updateAddressClick();
            }
        }
    }

    private class OnClickBeginUpdate implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.beginUpdateClick();
            }
        }
    }

    private class OnClickCancelUpdate implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.cancelUpdateClick();
            }
            dismiss();
        }
    }

    private class OnClickSubmitUpdate implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.submitUpdateClick();
            }
            dismiss();
        }
    }

    // 提供给外部使用的方法
    public DetailDialog setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
        return this;
    }

    public interface OnClickListener {
        void updateTimeClick();
        void updateAddressClick();
        void beginUpdateClick();
        void cancelUpdateClick();
        void submitUpdateClick();
    }
}
