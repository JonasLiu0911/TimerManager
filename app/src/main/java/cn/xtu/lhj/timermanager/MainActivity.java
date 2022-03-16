package cn.xtu.lhj.timermanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.poi.BaiduMapPoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    TextView locationInfo;

    private final String TAG = "MainActivity";

    public LocationClient mLocationClient;    // 定位对象

    MapView mMapView;
    BaiduMap mBaiduMap = null;

    boolean isFirstLocated = true;

    ImageView loginImg;
    Button startBtn;
    Button stopBtn;
    ListView locationsNearby;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        fullScreenConfig();
        setContentView(R.layout.activity_main);

        locationInfo = findViewById(R.id.location_info);

        // 对象初始化,声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        // 注册监听函数
        mLocationClient.registerLocationListener(new MyLocationListener());


        mMapView = findViewById(R.id.bd_map_view);
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);

        // 头像按钮
        loginImg = findViewById(R.id.go_to_login);
        OnClickHead onClick = new OnClickHead();
        loginImg.setOnClickListener(onClick);

        // 开始行程记录按钮
        startBtn = findViewById(R.id.start_log);
        OnClickStart onClickStart = new OnClickStart();
        startBtn.setOnClickListener(onClickStart);

        // 结束行程记录按钮
        stopBtn = findViewById(R.id.stop_log);
        OnClickStop onClickStop = new OnClickStop();
        stopBtn.setOnClickListener(onClickStop);

        // 周边POI列表
        locationsNearby = findViewById(R.id.location_nearby);


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
            // 调用LocationClient的start()方法，发起请求
            requestLocation();
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
                Toast.makeText(MainActivity.this, "开始行程记录", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 点击结束记录，停止行程记录（判断是否登录）
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
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
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

            Poi poi = bdLocation.getPoiList().get(0);
            String poiName = poi.getName();  // 获取poi名称
            String poiTags = poi.getTags();  // 获取poi类型
            String poiAddr = poi.getAddr();  // 获取poi地址 获取周边poi信息
            Log.d(TAG, poiName + "   " + poiTags + "   " + poiAddr);

            PoiRegion poiRegion = bdLocation.getPoiRegion();
            String poiDerectionDesc = poiRegion.getDerectionDesc();   // 获取poiRegion位置关系
            String poiRegionName = poiRegion.getName();               // 获取PoiRegion名称
            String poiRegionTags = poiRegion.getTags();               // 获取PoiRegion类型
            Log.d(TAG, poiDerectionDesc + "/" + poiRegionName + "/" + poiRegionTags);


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

            update = MapStatusUpdateFactory.zoomTo(20f);
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