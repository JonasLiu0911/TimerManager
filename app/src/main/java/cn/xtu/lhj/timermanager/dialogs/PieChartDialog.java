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
import cn.xtu.lhj.timermanager.view.PieChartView;

public class PieChartDialog extends AlertDialog {

    public TextView pieDate;
    public PieChartView pieChartView;

    public PieChartDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pie_chart);

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
        pieDate = findViewById(R.id.pie_date);
        pieChartView = findViewById(R.id.pie_chart);
    }
}
