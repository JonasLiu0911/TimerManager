package cn.xtu.lhj.timermanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;


public class BaseActivity extends AppCompatActivity {

    protected Toast toast;

    public BaiduMap mBaiduMap;
    // 定位对象
    public LocationClient mLocationClient;

    public MapView mMapView;

    public SharedPreferences sharedPreferences;

    // 是否首次定位
    public boolean isFirstLocated = true;

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
        runOnUiThread(() -> {
            toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();
        });
    }

    protected void hideToast() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }
}
