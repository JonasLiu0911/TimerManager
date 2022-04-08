package cn.xtu.lhj.timermanager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;
import com.xuexiang.xhttp2.reflect.TypeToken;


import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import cn.xtu.lhj.timermanager.adapter.GridAdapter;
import cn.xtu.lhj.timermanager.bean.Schedule;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.thread.UIRefresh;
import cn.xtu.lhj.timermanager.tools.AvatarImageView;

public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";

    // 相关按钮
    AvatarImageView loginImg;               // 头像按钮
    ImageView scheduleNotCheckImg;          // 事项选项按钮点击前
    ImageView scheduleCheckedImg;           // 事项选项按钮点击后
    ImageView newTrip;                      // 新建日程
    ImageView checkTrip;                    // 查看列表
    RelativeLayout rePopScheduleList;       // 事项列表
    ImageView packUpImg;                    // 收起

    private GridView gridView;
    private GridAdapter gridAdapter;
    private List<Schedule> results;
    private List<Schedule> scheduleList;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private String jsonToSave;
    private String jsonToGet;

    // 日期、时间选择相关
    private AddDialog addDialog;
    private DatePickerDialog.OnDateSetListener datePicker;
    private Calendar calendar;
    private Date date;

    // 地点选择相关
    private PickAddressDialog pickAddressDialog;
    private MapView mapViewInPick;
    private BaiduMap baiduMapInPick;
    private boolean isFirstLocatedInPick = true;
    private LocationClient locationClientInPick;


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
        List<String> permissionList = new ArrayList<>();
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
        checkTrip = findViewById(R.id.iv_check_trip);
        OnClickList onClickList = new OnClickList();
        checkTrip.setOnClickListener(onClickList);

        // 行程记录按钮
        newTrip = findViewById(R.id.iv_new_trip);
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

        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
        editor = sharedPreferences.edit();

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
            rePopScheduleList.setVisibility(View.INVISIBLE);
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

    // 定位初始化 ccc
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

    // 地点选择框中的定位初始化 ccc
    private void initLocationInPick() {

        baiduMapInPick.setMyLocationEnabled(true);

        // 定位初始化
        locationClientInPick = new LocationClient(getApplicationContext());

        // 注册监听函数
        locationClientInPick.registerLocationListener(new MyLocationListenerInPick());

        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("BD09LL");
        option.setScanSpan(2000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);

        locationClientInPick.setLocOption(option);
    }

    // 位置监听，定位到当前位置 ccc
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

    // 地点选择框中位置监听 ccc
    private class MyLocationListenerInPick extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) {
                return;
            }

            if (isFirstLocatedInPick) {
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                baiduMapInPick.animateMapStatus(update);

                update = MapStatusUpdateFactory.zoomTo(19.0f);
                baiduMapInPick.animateMapStatus(update);

                if (baiduMapInPick.getLocationData() != null) {
                    if (baiduMapInPick.getLocationData().latitude == bdLocation.getLatitude()
                            && baiduMapInPick.getLocationData().longitude == bdLocation.getLongitude()) {
                        isFirstLocatedInPick = false;
                    }
                }
            }

            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
            locationBuilder.longitude(bdLocation.getLongitude());
            locationBuilder.latitude(bdLocation.getLatitude());

            MyLocationData locationData = locationBuilder.build();

            baiduMapInPick.setMyLocationData(locationData);
        }
    }
    // ========================================== 定位相关 end ==========================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);    // 关闭定位图层
        mLocationClient.stop();                   // 停止定位服务
        mapViewInPick.onDestroy();
        baiduMapInPick.setMyLocationEnabled(false);
        locationClientInPick.stop();
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
        mapViewInPick.onPause();
    }

    // ========================================== 事项列表相关 end ==========================================

    private void initScheduleToShow() {

        gson = new Gson();
        results = new ArrayList<>();
        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
        asyncGetScheduleWithXHttp2(sharedPreferences.getString("telephone", ""));

        // 从sharedPreferences中取出日程信息List
        jsonToGet = sharedPreferences.getString("schedule_list", "");
        Type type = new TypeToken<List<Schedule>>() {}.getType();
        scheduleList = gson.fromJson(jsonToGet, type);

        if (scheduleList == null) {
            Log.d(TAG, "schedule list is empty");
        } else {
            Log.d(TAG, "schedule list is not empty");

            for (Schedule i : scheduleList) {
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
        }

    }

    public void asyncGetScheduleWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetScheduleURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<List<Schedule>>() {
                    @Override
                    public void onSuccess(List<Schedule> data) throws Throwable {

                        Log.d(TAG, "请求URL成功：" + data);

                        if (data != null) {

                            // 把日程信息List放入sharedPreferences中
                            editor = sharedPreferences.edit();
                            jsonToSave = gson.toJson(data);
                            editor.putString("schedule_list", jsonToSave);

                            if (editor.commit()) {
                                Log.d(TAG, "save success!!!");
                            } else {
                                Log.d(TAG, "save fail......");
                            }

                            results.addAll(data);

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



    // 添加日程按钮（判断是否登录）
    private class OnClickLog implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            setDateAndTimePickDialog();

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

                addDialog = new AddDialog(MainActivity.this, R.style.dialog);
                addDialog.setOnClickListener(new AddDialog.OnClickListener() {

                    /**
                     * 日期和时间选择
                     */
                    @Override
                    public void onPickTimeClick() {

                        // 隐藏软键盘
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(addDialog.btPickTime.getWindowToken(), 0);

                        // 弹出日期时间选择器（Dialog）
                        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                AlertDialog.THEME_HOLO_DARK,
                                datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.setTitle("请选择日期");
                        datePickerDialog.show();
                    }

                    /**
                     * 地点选择
                     */
                    @Override
                    public void onPickAddressClick() {

                        // 隐藏软键盘
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(addDialog.btPickAddress.getWindowToken(), 0);

                        // 弹出地图＋POI搜索栏（对话框Dialog），用户选择地点
                        pickAddressDialog = new PickAddressDialog(MainActivity.this, R.style.dialog_address);

                        // 地点选择框的点击事件
                        pickAddressDialog.setOnClickListener(new PickAddressDialog.OnClickListener() {
                            @Override
                            public void onToSearchClick() {
                                Toast.makeText(MainActivity.this, "搜索", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelAddressClick() {
                                Toast.makeText(MainActivity.this, "取消", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSubmitAddressClick() {
                                Toast.makeText(MainActivity.this, "提交", Toast.LENGTH_SHORT).show();
                            }
                        }).show();

                        mapViewInPick = pickAddressDialog.findViewById(R.id.map_view_in_dialog);
                        baiduMapInPick = mapViewInPick.getMap();
                        initLocationInPick();
                        locationClientInPick.start();

                        isFirstLocatedInPick = true;

                    }

                    /**
                     * 提交给后端
                     */
                    @Override
                    public void onSubmitClick() {

                        String inputTitle = addDialog.edtTitle.getText().toString();  // 事项标题（要提交）
                        String inputDesc = addDialog.edtDesc.getText().toString();    // 事项详情（要提交）

                        date = new Date();                                             // 事项时间（要提交）
                        date.setYear(sharedPreferences.getInt("year", 2000));
                        date.setMonth(sharedPreferences.getInt("month", 8));
                        date.setDate(sharedPreferences.getInt("day", 28));
                        date.setHours(sharedPreferences.getInt("hour", 12));
                        date.setMinutes(sharedPreferences.getInt("minute", 28));

                        Toast.makeText(MainActivity.this,inputTitle + inputDesc,Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, date+"", Toast.LENGTH_SHORT).show();

                        Toast.makeText(MainActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                        // 网络请求，将新建日程传到后端
                    }

                    @Override
                    public void onCancelClick() {
                        Toast.makeText(MainActivity.this,"取消添加",Toast.LENGTH_SHORT).show();
                    }
                }).show();
                addDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);  // 点击EditText 弹出软键盘
            }
        }
    }

    /**
     * 设置日期时间选择的对话框
     */
    public void setDateAndTimePickDialog() {
        calendar = Calendar.getInstance();
        datePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // 把日期值存入sharedPreferences中
                editor.putInt("year", year);
                editor.putInt("month", month);
                editor.putInt("day", dayOfMonth);
                editor.commit();

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        TimePickerDialog.THEME_HOLO_DARK,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // 把时间值存入sharedPreferences中
                                editor.putInt("hour", hourOfDay);
                                editor.putInt("minute", minute);
                                editor.commit();
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.setTitle("请选择具体时间");
                timePickerDialog.show();

            }
        };

    }
}