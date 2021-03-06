package cn.xtu.lhj.timermanager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
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
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import cn.xtu.lhj.timermanager.adapter.GridAdapter;
import cn.xtu.lhj.timermanager.bean.Location;
import cn.xtu.lhj.timermanager.bean.Schedule;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.dialogs.AddDialog;
import cn.xtu.lhj.timermanager.dialogs.DetailDialog;
import cn.xtu.lhj.timermanager.dialogs.PickAddressDialog;
import cn.xtu.lhj.timermanager.receiver.AlarmReceiver;
import cn.xtu.lhj.timermanager.utils.BDMapUtils;
import cn.xtu.lhj.timermanager.utils.DateUtils;
import cn.xtu.lhj.timermanager.utils.DistanceUtils;
import cn.xtu.lhj.timermanager.utils.NotificationUtils;
import cn.xtu.lhj.timermanager.utils.SPUtils;

public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MapView mMapView;
    private NotificationUtils mNotificationUtils;
    private Notification notification;
    // 是否首次定位
    public boolean isFirstLocated = true;

    // 相关按钮
    ShapeableImageView loginImg;            // 头像按钮
    ImageView newTrip;                      // 新建日程
    ImageView checkTrip;                    // 查看列表

    PopupWindow popupWindow;
    View contentView;

    private GridView gridView;
    private GridAdapter gridAdapter;
    private List<Schedule> scheduleList;
    private boolean isShowDelete = false;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private String jsonToGet;

    private Gson gsonFirst;
    private String jsonFirstGet;
    private boolean flag = true;
    private boolean isGetFirst = true;
    private List<Schedule> scheduleFirstList;
    private Schedule scheduleFirst;

    private AlarmManager alarmManager;

    private BitmapDescriptor bitmapDescriptor;

    private Boolean isGetTime = false;
    private Boolean isGetAddress = false;

    private Boolean isUpdatingTime = false;
    private Boolean isUpdatingAddress = false;

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

    // 日程详情查看相关
    private DetailDialog detailDialog;

    private MapView mapViewInDetail;
    private BaiduMap baiduMapInDetail;

    LatLng targetPoint;


    private RequestOptions requestOptions = RequestOptions.circleCropTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)//不做磁盘缓存
            .skipMemoryCache(true);//不做内存缓存


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        fullScreenConfig();
        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_main);

        initPage();

        initHead();

        // 地图初始化
        mMapView = findViewById(R.id.bd_map_view);
        mBaiduMap = mMapView.getMap();

        mMapView.showZoomControls(false);

        scheduleFirst = new Schedule();

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

            // 设置后台定位
            // android8.0及以上使用NotificationUtils
            if (Build.VERSION.SDK_INT >= 26) {
                mNotificationUtils = new NotificationUtils(this);
                Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification
                        ("适配android 8限制后台定位功能", "正在后台定位");
                notification = builder2.build();
            } else {
                // 获取一个Notification构造器
                Notification.Builder builder = new Notification.Builder(MainActivity.this);
                Intent nfIntent = new Intent(MainActivity.this, MainActivity.class);

                builder.setContentIntent(PendingIntent.
                        getActivity(MainActivity.this, 0, nfIntent, 0)) // 设置PendingIntent
                        .setContentTitle("适配android限制后台定位功能")         // 设置下拉列表里的标题
                        .setSmallIcon(R.drawable.logo)                      // 设置状态栏内的小图标
                        .setContentText("正在后台定位")                       // 设置上下文内容
                        .setWhen(System.currentTimeMillis());               // 设置该通知发生的时间

                notification = builder.build(); // 获取构建好的Notification
            }
            notification.defaults = Notification.DEFAULT_SOUND;             //设置为默认的声音
        }

        Boolean isLogin = sharedPreferences.getBoolean("isLogin", false);
        Boolean isGetLoc = sharedPreferences.getBoolean("isGetLoc", false);

        if (isLogin && isGetLoc) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {

                    Gson g = new Gson();
                    Type type = new TypeToken<LatLng>() {}.getType();
                    LatLng l = g.fromJson(sharedPreferences.getString("location_1_minute", ""), type);
                    BigDecimal longitude1Minute = BigDecimal.valueOf(l.longitude);
                    BigDecimal latitude1Minute = BigDecimal.valueOf(l.latitude);
                    Log.d("kkk", longitude1Minute + " " + latitude1Minute);

                    Date dateJudge = new Date(System.currentTimeMillis());
                    SimpleDateFormat dateFormatJudge = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Long timeS = DateUtils.getString2Time(dateFormatJudge.format(dateJudge), "yyyy-MM-dd HH:mm:ss");

                    // 在这里将数据 位置数据、用户手机号码、当前时间 传回服务器 !!!!!

                    Location location = new Location();
                    location.setTel(sharedPreferences.getString("telephone", "11"));
                    location.setLongitude(longitude1Minute);
                    location.setLatitude(latitude1Minute);
                    location.setTimeX(timeS);

                    String s = g.toJson(location);
                    asyncPostLocationWithXHttp2(s);

                    Log.d("kk", timeS + "ii");
                    Log.d("kk", sharedPreferences.getString("telephone", "11"));
                    Log.d("kkkk", s);
                }
            };
            timer.schedule(timerTask, 0, 60000);
        }

    }

    // 写将位置数据实时传给后端的方法
    private void asyncPostLocationWithXHttp2(final String jsonStr) {
        XHttp.post(NetConstant.getPostLocationURL())
                .params("jsonStr", jsonStr)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) {
                        Log.d("post", jsonStr);
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString() + "iii");
                    }
                });
    }

    // 页面控件及其显示 初始化所有重要变量
    private void initPage() {

        setPopWindow();

        // 头像按钮
        loginImg = findViewById(R.id.go_to_login);
        OnClickHead onClick = new OnClickHead();
        loginImg.setOnClickListener(onClick);

        // 事项列表按钮
        checkTrip = findViewById(R.id.iv_check_trip);
        OnClickList onClickList = new OnClickList();
        checkTrip.setOnClickListener(onClickList);

        // 行程记录按钮
        newTrip = findViewById(R.id.iv_new_trip);
        OnClickLog onClickLog = new OnClickLog();
        newTrip.setOnClickListener(onClickLog);

        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initHead();

        initScheduleToShow();
    }

    // 加载用户头像
    private void initHead() {
        String imageUrl = SPUtils.getString("imageUrl",null,MainActivity.this);
        if(imageUrl != null) {
            Glide.with(MainActivity.this).load(imageUrl).apply(requestOptions).into(loginImg);
        } else {
            loginImg.setImageResource(R.drawable.default_head);
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
                popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
            }

        }
    }

    private void setPopWindow() {
        contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_window, null);
        ImageView imageViewInPop = contentView.findViewById(R.id.button_hidden);
        if (scheduleFirst != null) {
            imageViewInPop.setOnClickListener(v -> {
                gridAdapter.setIsShowDelete(false);
                popupWindow.dismiss();
            });
        } else {
            imageViewInPop.setOnClickListener(v -> popupWindow.dismiss());
        }

        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.pop_window_anim_style);
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

                    // 设置后台定位
                    // android8.0及以上使用NotificationUtils
                    if (Build.VERSION.SDK_INT >= 26) {
                        mNotificationUtils = new NotificationUtils(this);
                        Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification
                                ("适配android 8限制后台定位功能", "正在后台定位");
                        notification = builder2.build();
                    } else {
                        // 获取一个Notification构造器
                        Notification.Builder builder = new Notification.Builder(MainActivity.this);
                        Intent nfIntent = new Intent(MainActivity.this, MainActivity.class);

                        builder.setContentIntent(PendingIntent.
                                getActivity(MainActivity.this, 0, nfIntent, 0))  // 设置PendingIntent
                                .setContentTitle("适配android限制后台定位功能")                               // 设置下拉列表里的标题
                                .setSmallIcon(R.drawable.logo)                                            // 设置状态栏内的小图标
                                .setContentText("正在后台定位")                                             // 设置上下文内容
                                .setWhen(System.currentTimeMillis());                                     // 设置该通知发生的时间

                        notification = builder.build();                             // 获取构建好的Notification
                    }
                    notification.defaults = Notification.DEFAULT_SOUND;             //设置为默认的声音
                } else {
                    Log.d(TAG, "发生未知错误");
                    finish();
                }
                break;
        }
    }

    // 首页的定位初始化 ccc
    private void initLocation() {

        mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        mLocationClient = new LocationClient(getApplicationContext());

        // 注册监听函数
        mLocationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("BD09LL");
        option.setScanSpan(5000);
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
                setTranslateAnimation(pickAddressDialog.movePickPoint);
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
                pointLongitude = centerPoint.longitude;
                pointLatitude = centerPoint.latitude;
                Log.d("to_save_data", "经度：" + pointLongitude + "  纬度：" + pointLatitude);
                // 把选择好的位置信息存入sharedPreferences中
                LatLng locationToStock = new LatLng(pointLatitude, pointLongitude);
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

    // 图片上下浮动
    private void setTranslateAnimation(ImageView iv) {
        ValueAnimator animator = ValueAnimator.ofInt(0, -25, 0);
        animator.addUpdateListener(animation -> {
            int currentValue = (Integer) animation.getAnimatedValue();
            System.out.println(currentValue);
            iv.setTranslationY(currentValue);
            iv.requestLayout();
        });
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(1);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500);
        animator.start();
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

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(point).zoom(19.5f);
        baiduMapInDetail.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        detailDialog.detailPoint.setVisibility(View.VISIBLE);

    }

    // 首页的位置监听，定位到当前位置 ccc
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

                update = MapStatusUpdateFactory.zoomTo(18.0f);
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

            Gson gsonWatching = new Gson();

            LatLng watchingLocation = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            editor.putString("location_1_minute", gsonWatching.toJson(watchingLocation));
            editor.putBoolean("isGetLoc", true);
            editor.commit();

            getFirstSchedule(bdLocation.getLongitude(), bdLocation.getLatitude());
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

        asyncGetScheduleWithXHttp2(sharedPreferences.getString("telephone", ""));

        // 从sharedPreferences中取出日程信息List
        jsonToGet = sharedPreferences.getString("schedule_list", "");
        Type type = new TypeToken<List<Schedule>>() {}.getType();
        scheduleList = gson.fromJson(jsonToGet, type);

        if (scheduleList != null) {
            getScheduleFirstItem();
        }


    }

    // 获取日程列表中的第一个日程
    private void getScheduleFirstItem() {

        gsonFirst = new Gson();

        asyncGetScheduleWithXHttp22(sharedPreferences.getString("telephone", ""));

    }

    private void getFirstSchedule(double longitudeCurrent, double latitudeCurrent) {
        if (isGetFirst) {
            getScheduleFirstItem();
            isGetFirst = false;
        }

        jsonFirstGet = sharedPreferences.getString("schedule_first", "");
        Type type = new TypeToken<List<Schedule>>() {}.getType();
        scheduleFirstList = gsonFirst.fromJson(jsonFirstGet, type);
        if (scheduleFirstList != null) {
            scheduleFirst = scheduleFirstList.get(0);
        } else {
            scheduleFirst = null;
        }

        if (scheduleFirst != null) {
            Date dateJudge = new Date(System.currentTimeMillis());
            SimpleDateFormat dateFormatJudge = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long timeString = DateUtils.getString2Time(dateFormatJudge.format(dateJudge), "yyyy-MM-dd HH:mm:ss");

            LatLng currentPoint = new LatLng(latitudeCurrent, longitudeCurrent);
            targetPoint = new LatLng(scheduleFirst.getLatitude().doubleValue(), scheduleFirst.getLongitude().doubleValue());

            // 计算两点间距离
            double dis = DistanceUtils.calculateDistance(targetPoint.latitude, targetPoint.longitude, currentPoint.latitude, currentPoint.longitude);
            Log.d("distance", dis + "--");

            // 判断时间是否到达列表最早日程开始时间点 前十分钟
            if (scheduleFirst.getScheduleStartTime() - timeString > 0 && scheduleFirst.getScheduleStartTime() - timeString <= 1000 * 60 * 10) {

                if (dis >= 300 && flag) {

                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent0 = new Intent(MainActivity.this, AlarmReceiver.class);
                    PendingIntent pendingIntent0 = PendingIntent.getBroadcast(MainActivity.this, 0, intent0, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent0);

                    // 创建通知
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification1;

                    Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.app_logo)
                            .setContentTitle(scheduleFirst.getScheduleInfo())
                            .setContentText(scheduleFirst.getScheduleTitle())
                            .setWhen(System.currentTimeMillis())
                            .setContentIntent(pendingIntent)
                            .setDefaults(Notification.DEFAULT_ALL);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("to-do", "待办事项", NotificationManager.IMPORTANCE_HIGH);
                        channel.enableVibration(true);
                        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                        channel.setVibrationPattern(new long[]{500});
                        manager.createNotificationChannel(channel);

                        builder.setChannelId("to-do");
                    }
                    notification1 = builder.build();

                    manager.notify(1, notification1);

                    flag = false;

                } else if (dis < 300 && flag) {

                    // 创建通知
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification1;

                    Intent intent = new Intent(MainActivity.this, ArrivedActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.app_logo)
                            .setContentTitle(scheduleFirst.getScheduleTitle())
                            .setContentText(scheduleFirst.getScheduleInfo())
                            .setWhen(System.currentTimeMillis())
                            .setContentIntent(pendingIntent)
                            .setDefaults(Notification.DEFAULT_ALL);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("to-do", "待办日程", NotificationManager.IMPORTANCE_HIGH);
                        channel.enableVibration(true);
                        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                        channel.setVibrationPattern(new long[]{500});
                        manager.createNotificationChannel(channel);

                        builder.setChannelId("to-do");
                    }
                    notification1 = builder.build();

                    manager.notify(1, notification1);

                    flag = false;
                }

            } else if (scheduleFirst.getScheduleStartTime() - timeString < 0) {
                new Thread(() -> runOnUiThread(() -> fresh())).start();
            }
        }

    }

    // 刷新方法内操作（还有没有更好的！！！）
    private void fresh() {
        try {
            Thread.sleep(1000);
            initScheduleToShow();
            isGetFirst = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void asyncGetScheduleWithXHttp22(String telephone) {
        XHttp.post(NetConstant.getGetScheduleURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<List<Schedule>>() {
                    @Override
                    public void onSuccess(List<Schedule> data) {

                        Log.d(TAG, "请求URL成功：" + data);
                        if (data != null) {
                            editor.putString("schedule_first", gsonFirst.toJson(data));
                            if (editor.commit()) {
                                Log.d(TAG, "save first success!!!");
                            } else {
                                Log.d(TAG, "save first fail......");
                            }
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        editor.putString("schedule_first", gsonFirst.toJson((Object) null));
                        editor.commit();
                        Log.d(TAG, e.getMessage());
                    }
                });
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
                    public void onSuccess(List<Schedule> data) {

                        Log.d(TAG, "请求URL成功：" + data);
                        if (data != null) {
                            // 把日程信息List放入sharedPreferences中
                            editor.putString("schedule_list", gson.toJson(data));
                            if (editor.commit()) {
                                Log.d(TAG, "save success!!!");
                            } else {
                                Log.d(TAG, "save fail......");
                            }
                            setDateAndTimePickDialog();

                            gridView = contentView.findViewById(R.id.schedule_pop_list);
                            gridAdapter = new GridAdapter(MainActivity.this, data, onClickListener);  // 实例化适配器
                            gridView.setAdapter(gridAdapter);

                            gridView.setOnItemClickListener((parent, view, position, id) -> {

                                gridAdapter.setIsShowDelete(false);

                                Schedule scheduleDetail = gridAdapter.getItem(position);
                                detailScheduleId = scheduleDetail.getId();

                                Gson gsonUpdateLoc = new Gson();

                                Date dateTemp = new Date(scheduleDetail.getScheduleStartTime());

                                editor.putInt("year", dateTemp.getYear());
                                editor.putInt("month", dateTemp.getMonth());
                                editor.putInt("day", dateTemp.getDate());
                                editor.putInt("hour", dateTemp.getHours());
                                editor.putInt("minute", dateTemp.getMinutes());

                                LatLng locationDe = new LatLng(scheduleDetail.getLatitude().doubleValue(), scheduleDetail.getLongitude().doubleValue());
                                editor.putString("location_save", gsonUpdateLoc.toJson(locationDe));

                                editor.commit();

                                // 在这里写，弹出日程详情的dialog
                                detailDialog = new DetailDialog(MainActivity.this, R.style.dialog);

                                detailDialog.setOnClickListener(new DetailDialog.OnClickListener() {
                                    @Override
                                    public void updateTimeClick() {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);
                                        // 弹出日期时间选择器（Dialog）

                                        isUpdatingTime = true;

                                        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                                AlertDialog.THEME_HOLO_DARK,
                                                datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                                        datePickerDialog.setTitle("修改日期");
                                        datePickerDialog.show();
                                    }

                                    @Override
                                    public void updateAddressClick() {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);

                                        isUpdatingAddress = true;

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
                                                isUpdatingAddress = false;
                                            }

                                            @Override
                                            public void onSubmitAddressClick() {

                                                editor.commit();

                                                if (isUpdatingAddress) {
                                                    String temp = sharedPreferences.getString("location_save", "");
                                                    Type type = new TypeToken<LatLng>() {}.getType();
                                                    LatLng haha = gson.fromJson(temp, type);
                                                    initLocationInDetail(haha.longitude, haha.latitude);
                                                    isUpdatingAddress = false;
                                                }
                                            }
                                        }).show();
                                        pickAddressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);  // 点击EditText 弹出软键盘

                                        initLocationInPick();
                                        locationClientInPick.start();

                                        isFirstLocatedInPick = true;
                                    }

                                    @Override
                                    public void beginUpdateClick() {
                                        detailDialog.initEvent();
                                        detailDialog.setCanceledOnTouchOutside(false);
                                    }

                                    @Override
                                    public void cancelUpdateClick() {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);
                                    }

                                    @Override
                                    public void submitUpdateClick() {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);

                                        // 取出电话号码
                                        String telephone1 = sharedPreferences.getString("telephone", "");
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
                                        scheduleToUpdate.setTelephone(telephone1);
                                        scheduleToUpdate.setLongitude(longitudeToServer);
                                        scheduleToUpdate.setLatitude(latitudeToServer);
                                        scheduleToUpdate.setScheduleTitle(inputTitle);
                                        scheduleToUpdate.setScheduleInfo(inputDesc);
                                        scheduleToUpdate.setScheduleStartTime(timeString);

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
                                initLocationInDetail(scheduleDetail.getLongitude().doubleValue(), scheduleDetail.getLatitude().doubleValue());
                            });

                            gridView.setOnItemLongClickListener((parent, view, position, id) -> {

                                if (gridAdapter.getIsShowDelete()) {
                                    isShowDelete = false;
                                } else {
                                    isShowDelete = true;
                                }
                                gridAdapter.setIsShowDelete(isShowDelete);

                                return true;
                            });
                        }

                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                        editor.putString("schedule_first", gsonFirst.toJson((Object) null));
                        editor.commit();
                        gridView = contentView.findViewById(R.id.schedule_pop_list);
                        gridView.setAdapter(null);
//                        showToastInThread(MainActivity.this, e.getMessage());
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
                    public void onSuccess(Object response) {
                        Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();

                        new Thread(() -> runOnUiThread(() -> fresh())).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
//                        showToastInThread(MainActivity.this, e.getMessage());
                    }
                });
    }

    private void asyncUpdateScheduleWithXHttp2(final String jsonStr) {
        XHttp.post(NetConstant.getUpdateScheduleURL())
                .params("jsonStr", jsonStr)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) {
                        Toast.makeText(MainActivity.this, "修改成功", Toast.LENGTH_SHORT).show();

                        new Thread(() -> runOnUiThread(() -> fresh())).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
//                        showToastInThread(MainActivity.this, e.getMessage());
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
                    public void onSuccess(Object response) {
                        Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();

                        new Thread(() -> runOnUiThread(() -> fresh())).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
//                        showToastInThread(MainActivity.this, e.getMessage());
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
                        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", (dialog, which) ->
                                Toast.makeText(MainActivity.this, "必须选择时间，请重新进入", Toast.LENGTH_SHORT).show());
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
                                isGetAddress = true;

                                BDMapUtils.reverseGeoParse(pointLongitude, pointLatitude, new OnGetGeoCoderResultListener() {
                                    @Override
                                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                                    }

                                    @Override
                                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                        addDialog.btPickAddress.setText(reverseGeoCodeResult.getAddress() + "\n" + reverseGeoCodeResult.getSematicDescription());
                                    }
                                });
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
                        // 事项详情（要提交）
                        String inputDesc = addDialog.edtDesc.getText().toString();

                        // 事项时间（要提交）
                        date = new Date();
                        date.setYear(sharedPreferences.getInt("year", 2000));
                        date.setMonth(sharedPreferences.getInt("month", 8));
                        date.setDate(sharedPreferences.getInt("day", 28));
                        date.setHours(sharedPreferences.getInt("hour", 12));
                        date.setMinutes(sharedPreferences.getInt("minute", 28));
                        date.setSeconds(0);

                        @SuppressLint("SimpleDateFormat")
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
                        Schedule scheduleToServer = new Schedule();
                        scheduleToServer.setTelephone(telephone);
                        scheduleToServer.setLongitude(longitudeToServer);
                        scheduleToServer.setLatitude(latitudeToServer);
                        scheduleToServer.setScheduleTitle(inputTitle);
                        scheduleToServer.setScheduleInfo(inputDesc);
                        scheduleToServer.setScheduleStartTime(timeString);

                        if (scheduleToServer.getScheduleTitle().length() == 0
                                || scheduleToServer.getScheduleInfo().length() == 0
                                || !isGetTime
                                || !isGetAddress) {
                            Toast.makeText(MainActivity.this, "日程设置不能为空", Toast.LENGTH_SHORT).show();
                        } else {

                            isGetTime = false;
                            isGetAddress = false;
                            addDialog.dismiss();
                            gsonSaveSchedule = new Gson();
                            jsonSaveSchedule = gsonSaveSchedule.toJson(scheduleToServer);

                            asyncAddScheduleWithXHttp2(jsonSaveSchedule);
                        }

                    }

                    @Override
                    public void onCancelClick() {
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
        datePicker = (view, year, month, dayOfMonth) -> {

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
                    (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        // 把时间值存入sharedPreferences中
                        editor.putInt("hour", hourOfDay);
                        editor.putInt("minute", minute);
                        editor.commit();
                        isGetTime = true;

                        // 事项时间（要提交）
                        Date date11 = new Date();
                        date11.setYear(sharedPreferences.getInt("year", 2000));
                        date11.setMonth(sharedPreferences.getInt("month", 8));
                        date11.setDate(sharedPreferences.getInt("day", 28));
                        date11.setHours(sharedPreferences.getInt("hour", 12));
                        date11.setMinutes(sharedPreferences.getInt("minute", 28));
                        date11.setSeconds(0);
                        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        addDialog.btPickTime.setText(f.format(date11));
                        if (isUpdatingTime) {
                            detailDialog.detailTimeStr.setText(f.format(date11));
                            isUpdatingTime = false;
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.setTitle("请选择具体时间");

            timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", (dialog, which) -> {
                isUpdatingTime = false;
                Toast.makeText(MainActivity.this, "必须选择具体时间，请重新进入", Toast.LENGTH_SHORT).show();
            });
            timePickerDialog.show();

        };

    }
}