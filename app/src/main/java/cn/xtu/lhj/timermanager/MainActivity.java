package cn.xtu.lhj.timermanager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;


import java.util.ArrayList;
import java.util.List;

import cn.xtu.lhj.timermanager.adapter.GridAdapter;
import cn.xtu.lhj.timermanager.bean.Schedule;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.tools.AvatarImageView;

public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";

    // 相关按钮
    AvatarImageView loginImg;                 // 头像按钮
    AvatarImageView scheduleNotCheckImg;      // 事项选项按钮点击前
    AvatarImageView scheduleCheckedImg;       // 事项选项按钮点击后
    LinearLayout newTrip;                     // 新建日程
    LinearLayout checkTrip;                   // 查看列表
    RelativeLayout rePopScheduleList;         // 事项列表
    ImageView packUpImg;                      // 收起

    private GridView gridView;
    private GridAdapter gridAdapter;
    public List<Schedule> results;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        fullScreenConfig();
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        initPage();

        // 地图初始化
        mMapView = findViewById(R.id.bd_map_view);
        mBaiduMap = mMapView.getMap();

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
        }

    }

    // 页面控件及其显示 初始化
    private void initPage() {
        // 头像按钮
        loginImg = findViewById(R.id.go_to_login);
        OnClickHead onClick = new OnClickHead();
        loginImg.setOnClickListener(onClick);

        scheduleNotCheckImg = findViewById(R.id.ai_schedule_not_check);
        OnClickNotCheck clickNotCheck = new OnClickNotCheck();
        scheduleNotCheckImg.setOnClickListener(clickNotCheck);

        scheduleCheckedImg = findViewById(R.id.ai_schedule_checked);
        OnClickChecked clickChecked = new OnClickChecked();
        scheduleCheckedImg.setOnClickListener(clickChecked);

        // 事项列表按钮
        checkTrip = findViewById(R.id.ll_check_trip);
        checkTrip.getBackground().mutate().setAlpha(220);
        OnClickList onClickList = new OnClickList();
        checkTrip.setOnClickListener(onClickList);

        // 行程记录按钮
        newTrip = findViewById(R.id.ll_new_trip);
        newTrip.getBackground().mutate().setAlpha(220);
        OnClickLog onClickLog = new OnClickLog();
        newTrip.setOnClickListener(onClickLog);

        // 事项列表容器
        rePopScheduleList = findViewById(R.id.re_pop_after_check);

        packUpImg = findViewById(R.id.pack_up_list);
        OnClickPackUp clickPackUp = new OnClickPackUp();
        packUpImg.setOnClickListener(clickPackUp);

        scheduleCheckedImg.setVisibility(View.INVISIBLE);
        checkTrip.setVisibility(View.INVISIBLE);
        newTrip.setVisibility(View.INVISIBLE);
        rePopScheduleList.setVisibility(View.INVISIBLE);

        initScheduleToShow();
    }

    // ========================================== 按钮相关 begin ==========================================
    // 点击事项选项展示按钮
    private class OnClickNotCheck implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            scheduleNotCheckImg.setVisibility(View.INVISIBLE);
            scheduleCheckedImg.setVisibility(View.VISIBLE);
            newTrip.setVisibility(View.VISIBLE);
            checkTrip.setVisibility(View.VISIBLE);
        }
    }

    // 点击收起按钮
    private class OnClickPackUp implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            rePopScheduleList.setVisibility(View.INVISIBLE);
        }
    }

    // 点击事项选项隐藏按钮
    private class OnClickChecked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            scheduleCheckedImg.setVisibility(View.INVISIBLE);
            scheduleNotCheckImg.setVisibility(View.VISIBLE);
            newTrip.setVisibility(View.INVISIBLE);
            checkTrip.setVisibility(View.INVISIBLE);
        }
    }

    // 点击头像跳转登录页---------加拦截器
    private class OnClickHead implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (sharedPreferences.getString("telephone", "") == "") {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);

                    scheduleCheckedImg.setVisibility(View.INVISIBLE);
                    scheduleNotCheckImg.setVisibility(View.VISIBLE);
                    newTrip.setVisibility(View.INVISIBLE);
                    checkTrip.setVisibility(View.INVISIBLE);

                    rePopScheduleList.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "当前按钮无效", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                if (intent != null) {
                    startActivity(intent);

                    scheduleCheckedImg.setVisibility(View.INVISIBLE);
                    scheduleNotCheckImg.setVisibility(View.VISIBLE);
                    newTrip.setVisibility(View.INVISIBLE);
                    checkTrip.setVisibility(View.INVISIBLE);

                    rePopScheduleList.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "当前按钮无效", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    // 点击事项列表
    private class OnClickList implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (sharedPreferences.getString("telephone", "") == "") {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "当前按钮无效", Toast.LENGTH_SHORT).show();
                }
            } else {
                rePopScheduleList.setVisibility(View.VISIBLE);
            }

        }
    }

    // 新建行程按钮（判断是否登录）
    private class OnClickLog implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (sharedPreferences.getString("telephone", "") == "") {
                Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "当前按钮无效", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "行程记录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ToDoListActivity.class);
                startActivity(intent);
            }
        }
    }
    // ========================================== 按钮相关 end ==========================================


    // ========================================== 定位相关 begin ==========================================

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

    // 定位初始化（需通用）
    private void initLocation() {

        mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        mLocationClient = new LocationClient(getApplicationContext());

        // 注册监听函数
        mLocationClient.registerLocationListener(new MyLocationListener());

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
        option.setScanSpan(2000);

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

        // 设置返回结果包含手机方向
        option.setNeedDeviceDirect(true);

        // 设置是否需要位置语义化结果
        option.setIsNeedLocationDescribe(true);

        // 设置是否需要POI结果
        option.setIsNeedLocationPoiList(true);

        mLocationClient.setLocOption(option);
    }

    // 位置监听，定位到当前位置（需通用）
    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) {
                return;
            }

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
    }
    // ========================================== 定位相关 end ==========================================

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

    // ========================================== 事项列表相关 end ==========================================

    private void initScheduleToShow() {
        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
        asyncGetScheduleWithXHttp2(sharedPreferences.getString("telephone", ""));
    }

    private void asyncGetScheduleWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetScheduleURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<List<Schedule>>() {
                    @Override
                    public void onSuccess(List<Schedule> data) throws Throwable {
                        Log.d(TAG, "请求URL成功：" + data);
                        if (data != null) {

                            results = new ArrayList<>(data.size());

                            Log.d(TAG, "data size is " + data.size());
                            results.addAll(data);

                            for (Schedule i : results) {
                                Log.d(TAG, "title is " + i.getScheduleTitle());
                                Log.d(TAG, "info is " + i.getScheduleInfo());
                                Log.d(TAG, "startTime is " + i.getScheduleStartTime());
                                Log.d(TAG, "id is " + i.getId());
                                Log.d(TAG, "userId is " + i.getUserId());
                                Log.d(TAG, "latitude is " + i.getLatitude());
                                Log.d(TAG, "longitude is " + i.getLongitude());
                                Log.d(TAG, "createTime is " + i.getCreateTime());
                                Log.d(TAG, "updateTime is " + i.getUpdateTime());
                            }

                            Toast.makeText(MainActivity.this, "haha", Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "results size is " + results.size());

                            if (results == null) {
                                Log.d(TAG, "null null null");
                            } else {
                                Log.d(TAG, "not null not null");
                            }

                            gridView = findViewById(R.id.grid_view_main);

                            gridAdapter = new GridAdapter(MainActivity.this, results);  // 实例化适配器

                            gridView.setAdapter(gridAdapter);
                        }

                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(MainActivity.this, e.getMessage());
                    }
                });
    }
}