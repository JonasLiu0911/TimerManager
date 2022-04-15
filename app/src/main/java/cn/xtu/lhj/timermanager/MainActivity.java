package cn.xtu.lhj.timermanager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;
import com.xuexiang.xhttp2.reflect.TypeToken;


import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.xtu.lhj.timermanager.adapter.GridAdapter;
import cn.xtu.lhj.timermanager.bean.Schedule;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.dialogs.AddDialog;
import cn.xtu.lhj.timermanager.dialogs.DetailDialog;
import cn.xtu.lhj.timermanager.dialogs.PickAddressDialog;
import cn.xtu.lhj.timermanager.utils.BDMapUtils;
import cn.xtu.lhj.timermanager.utils.DateUtils;
import cn.xtu.lhj.timermanager.utils.NotificationUtils;
import cn.xtu.lhj.timermanager.utils.SPUtils;

public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MapView mMapView;
    private MyLocationListener myLocationListener = new MyLocationListener();
    private NotificationUtils mNotificationUtils;
    private Notification notification;
    // 是否首次定位
    public boolean isFirstLocated = true;
    private boolean isEnableLocInForeground = false;

    // 相关按钮
    ShapeableImageView loginImg;            // 头像按钮
    ImageView scheduleNotCheckImg;          // 事项选项按钮点击前
    ImageView scheduleCheckedImg;           // 事项选项按钮点击后
    ImageView newTrip;                      // 新建日程
    ImageView checkTrip;                    // 查看列表
    RelativeLayout rePopScheduleList;       // 事项列表
    ImageView packUpImg;                    // 收起
    ImageView toBackStage;

    private GridView gridView;
    private GridAdapter gridAdapter;
    private List<Schedule> scheduleList;
    private boolean isShowDelete = false;

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
    private TextView fitAddressName;
    private TextView fitAddressDesc;
    private MyLocationConfiguration.LocationMode mCurrentModeInPick;
    private boolean isFirstLocatedInPick = true;
    private LocationClient locationClientInPick;
    private Double pointLongitude;
    private Double pointLatitude;
    private Gson gsonSaveLocation;
    private String jsonLocationToSave;
    private String jsonLocationToGet;
    private LatLng latLngGet;

    private Gson gsonSaveSchedule;
    private String jsonSaveSchedule;

    private Integer detailScheduleId;
    private Gson gsonUpdateSchedule;
    private String jsonUpdateSchedule;

    // 日程详情查看相关
    private DetailDialog detailDialog;

    private MapView mapViewInDetail;
    private BaiduMap baiduMapInDetail;


    private LatLng latLngInPick;
    private String cityInPick;
    private GeoCoder geoCoderInPick;

    private RequestOptions requestOptions = RequestOptions.circleCropTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)//不做磁盘缓存
            .skipMemoryCache(true);//不做内存缓存


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        fullScreenConfig();
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        initPage();

        initHead();

        // 地图初始化
        mMapView = findViewById(R.id.bd_map_view);
        mBaiduMap = mMapView.getMap();
//        mBaiduMap.setMyLocationEnabled(true);

        mMapView.showZoomControls(false);

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

        // 跳转后台
        toBackStage = findViewById(R.id.iv_to_backstage);
//        OnClickToBack onClickToBack = new OnClickToBack();
//        toBackStage.setOnClickListener(onClickToBack);

        // 事项列表容器
        rePopScheduleList = findViewById(R.id.re_pop_after_check);

        packUpImg = findViewById(R.id.pack_up_list);
        OnClickPackUp clickPackUp = new OnClickPackUp();
        packUpImg.setOnClickListener(clickPackUp);

        scheduleCheckedImg.setVisibility(View.INVISIBLE);
        checkTrip.setVisibility(View.INVISIBLE);
        newTrip.setVisibility(View.INVISIBLE);
        toBackStage.setVisibility(View.INVISIBLE);
        rePopScheduleList.setVisibility(View.INVISIBLE);

        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initHead();

        initScheduleToShow();
    }

    private void initHead() {
        String imageUrl = SPUtils.getString("imageUrl",null,MainActivity.this);
        if(imageUrl != null) {
            Glide.with(MainActivity.this).load(imageUrl).apply(requestOptions).into(loginImg);
        } else {
            loginImg.setImageResource(R.drawable.default_head);
        }
    }

    // ========================================== 按钮相关 begin ==========================================
    // 跳转后台
    private class OnClickToBack implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mLocationClient != null) {
                if (isEnableLocInForeground) {
                    //关闭后台定位（true：通知栏消失；false：通知栏可手动划除）
                    mLocationClient.disableLocInForeground(true);
                    isEnableLocInForeground = false;
                    mLocationClient.stop();
                } else {
                    mLocationClient.enableLocInForeground(1, notification);
                    isEnableLocInForeground = true;
                    mLocationClient.start();
                }
            }
        }
    }

    // 点击事项选项展示按钮
    private class OnClickNotCheck implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            scheduleNotCheckImg.setVisibility(View.INVISIBLE);
            scheduleCheckedImg.setVisibility(View.VISIBLE);
            newTrip.setVisibility(View.VISIBLE);
            checkTrip.setVisibility(View.VISIBLE);
            toBackStage.setVisibility(View.VISIBLE);
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
            toBackStage.setVisibility(View.INVISIBLE);
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
                    toBackStage.setVisibility(View.INVISIBLE);

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
                    toBackStage.setVisibility(View.INVISIBLE);

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
                    Log.d(TAG, "发生未知错误");
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

        mLocationClient.setLocOption(option);
    }

    // 地点选择框中的定位初始化 ccc
    private void initLocationInPick() {
        mapViewInPick = pickAddressDialog.mvInDialog;
        baiduMapInPick = mapViewInPick.getMap();

        fitAddressName = pickAddressDialog.moveName;
        fitAddressDesc = pickAddressDialog.moveAddress;

        gsonSaveLocation = new Gson();
        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

        // 定义地图状态
        MapStatus mapStatusInPick = new MapStatus.Builder().zoom(18).build();
        MapStatusUpdate mapStatusUpdateInPick = MapStatusUpdateFactory.newMapStatus(mapStatusInPick);

        // 改变地图状态
        baiduMapInPick.setMapStatus(mapStatusUpdateInPick);

        // 地图状态改变相关监听
        baiduMapInPick.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                LatLng centerPoint = mapStatus.target;
                pointLongitude = (Double) centerPoint.longitude;
                pointLatitude = (Double) centerPoint.latitude;
                Log.d(TAG, centerPoint.latitude + "/" + centerPoint.longitude);
                // 把选择好的位置信息存入sharedPreferences中
                LatLng locationToStock = new LatLng(pointLatitude, pointLongitude);
                editor = sharedPreferences.edit();
                jsonLocationToSave = gsonSaveLocation.toJson(locationToStock);
                editor.putString("location_save", jsonLocationToSave);

                BDMapUtils.reverseGeoParse(pointLongitude, pointLatitude, new OnGetGeoCoderResultListener() {
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                    }

                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                        fitAddressName.setText(reverseGeoCodeResult.getAddress());
                        fitAddressDesc.setText(reverseGeoCodeResult.getSematicDescription());
                    }
                });
            }
        });

        // 开启定位图层
        baiduMapInPick.setMyLocationEnabled(true);
        // 定位图层显示方式
        mCurrentModeInPick = MyLocationConfiguration.LocationMode.NORMAL;
        // 设置定位图层配置信息
        baiduMapInPick.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentModeInPick, true, null));

        // 初始化定位
        locationClientInPick = new LocationClient(getApplicationContext());
        // 注册定位监听
        locationClientInPick.registerLocationListener(new MyLocationListenerInPick());

        LocationClientOption optionInPick = new LocationClientOption();
        optionInPick.setCoorType("BD09LL");
        optionInPick.setIsNeedAddress(true);
        optionInPick.setIsNeedLocationDescribe(true);
        optionInPick.setIsNeedLocationPoiList(true);

        optionInPick.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        optionInPick.setOpenGps(true);
        optionInPick.setScanSpan(1000);

        locationClientInPick.setLocOption(optionInPick);
        locationClientInPick.start();
    }

    // 详情框中定位初始化 ccc
    private void initLocationInDetail(double longitude, double latitude) {
        mapViewInDetail = detailDialog.detailAddressBaiduMap;
        baiduMapInDetail = mapViewInDetail.getMap();

        mapViewInDetail.showScaleControl(false);
        mapViewInDetail.showZoomControls(false);
        // 设置详情栏中的地图不可操作
        baiduMapInDetail.getUiSettings().setAllGesturesEnabled(false);

        baiduMapInDetail.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        mapViewInDetail.setLogoPosition(LogoPosition.logoPostionleftBottom);

        LatLng point = new LatLng(latitude, longitude);
        Log.d(TAG, point.latitude + "/" + point.longitude);

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(point).zoom(19.5f);
        baiduMapInDetail.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        detailDialog.detailPoint.setVisibility(View.VISIBLE);

        // 标记点加不上啊！！！呜呜呜
//        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_small);
//        OverlayOptions markerOptions = new MarkerOptions().position(point).icon(bitmapDescriptor);
//        baiduMapInDetail.addOverlay(markerOptions);
    }

    // 位置监听，定位到当前位置 ccc
    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mMapView == null) {
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

            MyLocationData locationData = new MyLocationData.Builder()
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();

            mBaiduMap.setMyLocationData(locationData);
            android.graphics.Point location = new android.graphics.Point(150, 300);
            mBaiduMap.setCompassPosition(location);

        }
    }

    // 地点选择框中位置监听 ccc
    private class MyLocationListenerInPick extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || baiduMapInPick == null) {
                return;
            }

            // 定位数据
            MyLocationData data = new MyLocationData.Builder()
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();

            // 设置定位数据
            baiduMapInPick.setMyLocationData(data);

            // 是否第一次定位
            if (isFirstLocatedInPick) {
                isFirstLocatedInPick = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
                baiduMapInPick.animateMapStatus(msu);
            }

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
        if (geoCoderInPick != null) {
            geoCoderInPick.destroy();
        }
        mapViewInPick = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        initHead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    // ========================================== 事项列表相关 end ==========================================

    private void initScheduleToShow() {

        gson = new Gson();
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
                Log.d(TAG, "telephone is " + sharedPreferences.getString("telephone", ""));
                Log.d(TAG, "latitude is " + i.getLatitude());
                Log.d(TAG, "longitude is " + i.getLongitude());
            }
        }

    }

    // 刷新方法内操作（还有没有更好的！！！）
    private void fresh() {
        try {
            Thread.sleep(1000);
            initScheduleToShow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 网络请求，通过手机号获取日程信息
     * @param telephone
     */
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

                            setDateAndTimePickDialog();

                            gridView = findViewById(R.id.grid_view_main);
                            gridAdapter = new GridAdapter(MainActivity.this, data, onClickListener);  // 实例化适配器
                            gridView.setAdapter(gridAdapter);

                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    Schedule scheduleDetail = gridAdapter.getItem(position);
                                    detailScheduleId = scheduleDetail.getId();

                                    // 在这里写，弹出日程详情的dialog
                                    detailDialog = new DetailDialog(MainActivity.this, R.style.dialog);

                                    detailDialog.setOnClickListener(new DetailDialog.OnClickListener() {
                                        @Override
                                        public void updateTimeClick() {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);
                                            // 弹出日期时间选择器（Dialog）
                                            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                                    AlertDialog.THEME_HOLO_DARK,
                                                    datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                                            datePickerDialog.setTitle("修改日期");
                                            datePickerDialog.show();
                                        }

                                        @Override
                                        public void updateAddressClick() {
//                                            Toast.makeText(MainActivity.this, "更新地址", Toast.LENGTH_SHORT).show();
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);

                                            // 弹出地图＋POI搜索栏（对话框Dialog），用户选择地点
                                            pickAddressDialog = new PickAddressDialog(MainActivity.this, R.style.dialog_address);

                                            // 地点选择框的点击事件（还未完成）
                                            pickAddressDialog.setOnClickListener(new PickAddressDialog.OnClickListener() {
                                                @Override
                                                public void onToSearchClick() {
                                                    InputMethodManager imm1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm1.hideSoftInputFromWindow(pickAddressDialog.ivToSearch.getWindowToken(), 0);
                                                    pickAddressDialog.cvMove.setVisibility(View.INVISIBLE);
                                                    pickAddressDialog.cvSearch.setVisibility(View.VISIBLE);
                                                    pickAddressDialog.cvMap.setVisibility(View.INVISIBLE);
                                                }

                                                @Override
                                                public void onCancelAddressClick() {
//                                                    Toast.makeText(MainActivity.this, "必须选择地点，请重新进入", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onSubmitAddressClick() {
                                                    editor.commit();
                                                }
                                            }).show();
                                            pickAddressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);  // 点击EditText 弹出软键盘

                                            initLocationInPick();
                                            locationClientInPick.start();

                                            isFirstLocatedInPick = true;
                                        }

                                        @Override
                                        public void beginUpdateClick() {
//                                            Toast.makeText(MainActivity.this, "开始更新", Toast.LENGTH_SHORT).show();
                                            detailDialog.initEvent();
                                            detailDialog.setCanceledOnTouchOutside(false);
                                        }

                                        @Override
                                        public void cancelUpdateClick() {
//                                            Toast.makeText(MainActivity.this, "取消修改", Toast.LENGTH_SHORT).show();
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);
                                        }

                                        @Override
                                        public void submitUpdateClick() {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);

                                            // 取出电话号码
                                            String telephone = sharedPreferences.getString("telephone", "");

                                            // 事项标题（要提交）
                                            String inputTitle = detailDialog.detailTitle.getText().toString();
                                            // 事项详情（要提交）
                                            String inputDesc = detailDialog.detailDesc.getText().toString();

                                            int year = sharedPreferences.getInt("year", 2000);
                                            int month = sharedPreferences.getInt("month", 8);
                                            int day = sharedPreferences.getInt("day", 28);
                                            int hour = sharedPreferences.getInt("hour", 12);
                                            int minute = sharedPreferences.getInt("minute", 28);
                                            int second = 0;
                                            // 事项时间（要提交）
                                            date = new Date();
                                            date.setYear(year);
                                            date.setMonth(month);
                                            date.setDate(day);
                                            date.setHours(hour);
                                            date.setMinutes(minute);
                                            date.setSeconds(second);
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            Long timeString = DateUtils.getString2Time(dateFormat.format(date), "yyyy-MM-dd HH:mm:ss");

                                            // 从sharedPreferences中取出用户选择的位置
                                            jsonLocationToGet = sharedPreferences.getString("location_save", "");
                                            Type type = new TypeToken<LatLng>() {}.getType();
                                            latLngGet = gson.fromJson(jsonLocationToGet, type);
                                            // 事项经度（要提交）
                                            BigDecimal longitudeToServer = BigDecimal.valueOf(latLngGet.longitude);
                                            // 事项纬度（要提交）
                                            BigDecimal latitudeToServer = BigDecimal.valueOf(latLngGet.latitude);

                                            // 网络请求，将新建日程传到后端
                                            Schedule scheduleToUpdate = new Schedule();
                                            scheduleToUpdate.setId(detailScheduleId);
                                            scheduleToUpdate.setTelephone(telephone);
                                            scheduleToUpdate.setLongitude(longitudeToServer);
                                            scheduleToUpdate.setLatitude(latitudeToServer);
                                            scheduleToUpdate.setScheduleTitle(inputTitle);
                                            scheduleToUpdate.setScheduleInfo(inputDesc);
                                            scheduleToUpdate.setScheduleStartTime(timeString);
                                            Log.d(TAG, "-------：" + scheduleToUpdate.getId());
                                            Log.d(TAG, "-------：" + scheduleToUpdate.getTelephone());
                                            Log.d(TAG, "-------：" + scheduleToUpdate.getLongitude());
                                            Log.d(TAG, "-------：" + scheduleToUpdate.getLatitude());
                                            Log.d(TAG, "-------：" + scheduleToUpdate.getScheduleTitle());
                                            Log.d(TAG, "-------：" + scheduleToUpdate.getScheduleInfo());
                                            Log.d(TAG, "-------：" + scheduleToUpdate.getScheduleStartTime());

                                            gsonSaveSchedule = new Gson();
                                            jsonSaveSchedule = gsonSaveSchedule.toJson(scheduleToUpdate);
                                            // 继续写，异步更新
                                            asyncUpdateScheduleWithXHttp2(jsonSaveSchedule);

                                        }
                                    }).show();
                                    detailDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                                    detailDialog.detailTitle.setText(scheduleDetail.getScheduleTitle());
                                    detailDialog.detailDesc.setText(scheduleDetail.getScheduleInfo());
                                    detailDialog.detailTimeStr.setText(DateUtils.getTime2String(scheduleDetail.getScheduleStartTime(), "yyyy-MM-dd HH:mm"));
                                    Log.d(TAG, scheduleDetail.getLongitude().doubleValue() + " " + scheduleDetail.getLatitude().doubleValue());
                                    initLocationInDetail(scheduleDetail.getLongitude().doubleValue(), scheduleDetail.getLatitude().doubleValue());
                                }
                            });

                            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                                    if (gridAdapter.getIsShowDelete()) {
                                        isShowDelete = false;
                                    } else {
                                        isShowDelete = true;
                                    }
                                    gridAdapter.setIsShowDelete(isShowDelete);

                                    return true;
                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this, "暂无日程", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(MainActivity.this, e.getMessage());
                    }
                });
    }


    // 子项中删除按钮 添加监听事件
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView deleteItem = (ImageView) v;
            int pos = (Integer) deleteItem.getTag();
            Schedule scheduleToDelete = gridAdapter.getItem(pos);
            Log.d(TAG, "id: " + scheduleToDelete.getId());
            Log.d(TAG, "title: " + scheduleToDelete.getScheduleTitle());
            Log.d(TAG, "删除键位置: " + pos);
            asyncDeleteScheduleWithXHttp2(scheduleToDelete.getId());

        }
    };


    /**
     * 网络请求，添加日程
     * @param jsonStr
     */
    private void asyncAddScheduleWithXHttp2(final String jsonStr) {
        XHttp.post(NetConstant.getAddScheduleURL())
                .params("jsonStr", jsonStr)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) throws Throwable {
                        Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fresh();
                                    }
                                });
                            }
                        }).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(MainActivity.this, e.getMessage());
                    }
                });
    }

    private void asyncUpdateScheduleWithXHttp2(final String jsonStr) {
        XHttp.post(NetConstant.getUpdateScheduleURL())
                .params("jsonStr", jsonStr)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) throws Throwable {
                        Toast.makeText(MainActivity.this, "修改成功", Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fresh();
                                    }
                                });
                            }
                        }).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        showToastInThread(MainActivity.this, e.getMessage());
                    }
                });
    }


    /**
     * 网络请求，通过日程id删除日程
     * @param id
     */
    private void asyncDeleteScheduleWithXHttp2(final Integer id) {
        XHttp.post(NetConstant.getDeleteScheduleURL())
                .params("scheduleId", id)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) throws Throwable {
                        Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fresh();
                                    }
                                });
                            }
                        }).start();
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
                        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "必须选择时间，请重新进入", Toast.LENGTH_SHORT).show();
                            }
                        });
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

                        // 地点选择框的点击事件（还未完成）
                        pickAddressDialog.setOnClickListener(new PickAddressDialog.OnClickListener() {
                            @Override
                            public void onToSearchClick() {
                                InputMethodManager imm1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm1.hideSoftInputFromWindow(pickAddressDialog.ivToSearch.getWindowToken(), 0);
                                pickAddressDialog.cvMove.setVisibility(View.INVISIBLE);
                                pickAddressDialog.cvSearch.setVisibility(View.VISIBLE);
                                pickAddressDialog.cvMap.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onCancelAddressClick() {
                                Toast.makeText(MainActivity.this, "必须选择地点，请重新进入", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSubmitAddressClick() {
                                editor.commit();
                            }
                        }).show();
                        pickAddressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);  // 点击EditText 弹出软键盘

                        initLocationInPick();
                        locationClientInPick.start();

                        isFirstLocatedInPick = true;

                    }

                    /**
                     * 提交给后端
                     */
                    @Override
                    public void onSubmitClick() {

                        // 取出电话号码
                        String telephone = sharedPreferences.getString("telephone", "");

                        // 事项标题（要提交）
                        String inputTitle = addDialog.edtTitle.getText().toString();
                        Log.d(TAG, "标题：" + inputTitle);
                        // 事项详情（要提交）
                        String inputDesc = addDialog.edtDesc.getText().toString();
                        Log.d(TAG, "详情：" + inputDesc);

                        int year = sharedPreferences.getInt("year", 2000);
                        int month = sharedPreferences.getInt("month", 8);
                        int day = sharedPreferences.getInt("day", 28);
                        int hour = sharedPreferences.getInt("hour", 12);
                        int minute = sharedPreferences.getInt("minute", 28);
                        int second = 0;

                        // 事项时间（要提交）
                        date = new Date();
                        date.setYear(year);
                        date.setMonth(month);
                        date.setDate(day);
                        date.setHours(hour);
                        date.setMinutes(minute);
                        date.setSeconds(second);
                        Log.d(TAG, date + "------");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Long timeString = DateUtils.getString2Time(dateFormat.format(date), "yyyy-MM-dd HH:mm:ss");

                        Log.d(TAG, "时间戳：" + timeString);
                        Log.d(TAG, "转换后时间：" + DateUtils.getTime2String(timeString, "yyyy-MM-dd HH:mm"));

                        // 从sharedPreferences中取出用户选择的位置
                        jsonLocationToGet = sharedPreferences.getString("location_save", "");
                        Type type = new TypeToken<LatLng>() {}.getType();
                        latLngGet = gson.fromJson(jsonLocationToGet, type);
                        // 事项经度（要提交）
                        BigDecimal longitudeToServer = BigDecimal.valueOf(latLngGet.longitude);
                        // 事项纬度（要提交）
                        BigDecimal latitudeToServer = BigDecimal.valueOf(latLngGet.latitude);

                        Log.d(TAG, "经度：" + longitudeToServer);
                        Log.d(TAG, "纬度：" + latitudeToServer);

                        // 网络请求，将新建日程传到后端
                        Schedule scheduleToServer = new Schedule();
                        scheduleToServer.setTelephone(telephone);
                        scheduleToServer.setLongitude(longitudeToServer);
                        scheduleToServer.setLatitude(latitudeToServer);
                        scheduleToServer.setScheduleTitle(inputTitle);
                        scheduleToServer.setScheduleInfo(inputDesc);
                        scheduleToServer.setScheduleStartTime(timeString);
                        Log.d(TAG, "-------：" + scheduleToServer.getScheduleStartTime());

                        gsonSaveSchedule = new Gson();
                        jsonSaveSchedule = gsonSaveSchedule.toJson(scheduleToServer);

                        asyncAddScheduleWithXHttp2(jsonSaveSchedule);

                    }

                    @Override
                    public void onCancelClick() {
//                        Toast.makeText(MainActivity.this,"取消添加",Toast.LENGTH_SHORT).show();
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
                editor.putInt("year", year - 1900);
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
                timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "必须选择具体时间，请重新进入", Toast.LENGTH_SHORT).show();
                    }
                });
                timePickerDialog.show();

            }
        };

    }
}