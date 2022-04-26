package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

public class ArrivedActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fullScreenConfig();
        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_arrived);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(1);

    }

    public void arrived(View view) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(0);
        Toast.makeText(ArrivedActivity.this, "已到达", Toast.LENGTH_SHORT).show();
        finish();
    }
}