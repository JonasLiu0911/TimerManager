package cn.xtu.lhj.timermanager.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;

import cn.xtu.lhj.timermanager.R;

public class HistoryDialog extends AlertDialog {

    public TextView historyTitle;
    public TextView historyDesc;
    public TextView historyTimeStr;

    public MapView historyAddress;

    public HistoryDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_history);

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
        historyTitle = findViewById(R.id.title_history);
        historyDesc = findViewById(R.id.desc_history);
        historyTimeStr = findViewById(R.id.time_history);

        historyAddress = findViewById(R.id.address_history);
    }
}
