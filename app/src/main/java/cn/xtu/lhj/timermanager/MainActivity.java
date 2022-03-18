package cn.xtu.lhj.timermanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.location.PoiRegion;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.poi.BaiduMapPoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    // 位置经纬度信息
    TextView locationInfo;

    private final String TAG = "MainActivity";

    // 定位对象
    public LocationClient mLocationClient;
//    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;

    MapView mMapView;
    BaiduMap mBaiduMap;

//    private SensorManager mSensorManager;
//    double degree = 0;

    boolean isFirstLocated = true;   // 是否首次定位

    ImageView loginImg;
    Button startBtn;
    Button stopBtn;

    /**
    // 轨迹相关
    boolean trace = false;
    boolean isFirstTrace = true;

    // 起点图标
    BitmapDescriptor startBD;
    // 终点图标
    BitmapDescriptor stopBD;
    // 位置点集合
    List<LatLng> points = new ArrayList<LatLng>();
    // 运动轨迹图层
    Polyline mPolyline;
    // 上一个定位点
    LatLng last = new LatLng(0, 0);
     */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        fullScreenConfig();
        setContentView(R.layout.activity_main);

        // 头像按钮
        loginImg = findViewById(R.id.go_to_login);
        OnClickHead onClick = new OnClickHead();
        loginImg.setOnClickListener(onClick);


        // 结束行程记录按钮
        stopBtn = findViewById(R.id.stop_log);
        OnClickStop onClickStop = new OnClickStop();
        stopBtn.setOnClickListener(onClickStop);

        // 地理位置信息初始化
        locationInfo = findViewById(R.id.location_info);
        locationInfo.setVisibility(View.INVISIBLE);


        // 地图初始化
        mMapView = findViewById(R.id.bd_map_view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        mLocationClient = new LocationClient(getApplicationContext());
        // 注册监听函数
        mLocationClient.registerLocationListener(new MyLocationListener());

        // 权限请求
        List<String> permissionList = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            initLocation();
            mLocationClient.start();


            // 开始行程记录按钮        // 轨迹
            startBtn = findViewById(R.id.start_log);
            OnClickStart onClickStart = new OnClickStart();
            startBtn.setOnClickListener(onClickStart);

        }

    }

    // 点击头像跳转登录页---------加拦截器
    private class OnClickHead implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SharedPreferences sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

            if (sharedPreferences.getString("telephone", "") == "") {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "当前按钮无效", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "当前按钮无效", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    // 点击开始记录，记录行程（判断是否登录）
    private class OnClickStart implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SharedPreferences sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

            if (sharedPreferences.getString("telephone", "") == "") {
                Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "当前按钮无效", Toast.LENGTH_SHORT).show();
                }
            } else {
                loginImg.setVisibility(View.INVISIBLE);
                locationInfo.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "开始行程记录", Toast.LENGTH_SHORT).show();

                /**
                if (isFirstTrace) {
                    points.clear();
                    last = new LatLng(0, 0);
                    return;
                }

                // 地图标记覆盖物参数配置类
                MarkerOptions oStop = new MarkerOptions();
                oStop.position(points.get(points.size() - 1));
                oStop.icon(stopBD);
                mBaiduMap.addOverlay(oStop);

                points.clear();
                last = new LatLng(0, 0);
                isFirstTrace = true;
                 */
            }
        }
    }

    /**
    private SensorEventListener listener = new SensorEventListener() {

        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];

        @Override
        public void onSensorChanged(SensorEvent event) {

            // 判断当前是加速度传感器还是地磁传感器
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone();
            }
            float[] R = new float[9];
            float[] values = new float[3];
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
            SensorManager.getOrientation(R, values);
            degree = Math.toDegrees(values[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
     */

    // 点击结束记录，结束行程记录（判断是否登录）
    private class OnClickStop implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SharedPreferences sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

            if (sharedPreferences.getString("telephone", "") == "") {
                Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "当前按钮无效", Toast.LENGTH_SHORT).show();
                }
            } else {
                loginImg.setVisibility(View.VISIBLE);
                locationInfo.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "结束行程记录", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 请求权限的结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用此程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    initLocation();
                    mLocationClient.start();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();

        // 可选，设置定位模式，默认高精度（Hight_Accuracy）
        // 低功耗（Battery_Saving）；仅使用设备（Device_Sensors）
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        // 可选，设置返回经纬度坐标类型，默认GCJ02
        // GCJ02：国测局坐标；BD09ll：百度经纬度坐标；BD09：百度墨卡托坐标
        // 海外地区定位，无需设置坐标类型，统一返回WGS84类型
        option.setCoorType("BD09LL");

        // 可选，设置发起定位请求的间隔，int类型，单位ms
        // 若设置为0，则代表单次定位，即仅定位一次，默认为0
        // 若设置非0，需设置1000ms以上才有效
        option.setScanSpan(1000);

        // 可选，设置是否使用gps，默认false
        // 使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);

        // 可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setLocationNotify(true);

        // 可选，定位SDK内部是一个service，并放到了独立进程
        // 设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.setIgnoreKillProcess(false);

        // 可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(false);

        // 可选，V7.2版本新增能力
        // 如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位
        option.setWifiCacheTimeOut(5 * 60 * 1000);

        // 可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        option.setEnableSimulateGps(false);

        // 设置是否需要地址信息
        option.setIsNeedAddress(true);

        // 设置是否需要位置语义化结果
        option.setIsNeedLocationDescribe(true);

        // 设置是否需要POI结果
        option.setIsNeedLocationPoiList(true);

        mLocationClient.setLocOption(option);
    }

    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) {
                return;
            }

            navigateTo(bdLocation);

            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
            locationInfo.setText(currentPosition);
        }
    }

    private void navigateTo(BDLocation bdLocation) {
        if (isFirstLocated) {
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);

            update = MapStatusUpdateFactory.zoomTo(19.0f);
            mBaiduMap.animateMapStatus(update);

            if (mBaiduMap.getLocationData() != null) {
                if (mBaiduMap.getLocationData().latitude == bdLocation.getLatitude()
                        && mBaiduMap.getLocationData().longitude == bdLocation.getLongitude()) {
                    isFirstLocated = false;
                }
            }
        }

        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.longitude(bdLocation.getLongitude());
        locationBuilder.latitude(bdLocation.getLatitude());

        MyLocationData locationData = locationBuilder.build();

        mBaiduMap.setMyLocationData(locationData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);    // 关闭定位图层
        mLocationClient.stop();                   // 停止定位服务
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}