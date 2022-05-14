package cn.xtu.lhj.timermanager.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.TextView;

import cn.xtu.lhj.timermanager.R;

public class AnalyzeDialog extends AlertDialog {

    public TextView analyzeDate;
    public GridView gvLists;

    public AnalyzeDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_result);

        setCanceledOnTouchOutside(true);

        setDialogPosition();

        initView();

    }

    private void setDialogPosition() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = getWindow();
        layoutParams.copyFrom(window.getAttributes());

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;

        window.setAttributes(layoutParams);
    }

    private void initView() {
        analyzeDate = findViewById(R.id.analyze_date);
        gvLists = findViewById(R.id.gv_anal_dialog);
    }
}
