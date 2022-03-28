package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

public class InfoChangeActivity extends AppCompatActivity {

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_change);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("信息修改");
        }
    }

    // 菜单点击实现
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Toast.makeText(InfoChangeActivity.this, "信息未保存，放弃修改", Toast.LENGTH_SHORT).show();
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}