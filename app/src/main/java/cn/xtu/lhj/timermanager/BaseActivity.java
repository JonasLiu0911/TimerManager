package cn.xtu.lhj.timermanager;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected Toast toast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    // 全屏显示
    protected void fullScreenConfig() {
        // 去除ActionBar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    // 实现在子线程中显示Toast
    protected void showToastInThread(Context context, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    protected void hideToast() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }
}
