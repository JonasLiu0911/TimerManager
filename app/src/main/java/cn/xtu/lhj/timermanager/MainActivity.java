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
    // ??????????????????
    public boolean isFirstLocated = true;

    // ????????????
    ShapeableImageView loginImg;            // ????????????
    ImageView newTrip;                      // ????????????
    ImageView checkTrip;                    // ????????????

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

    // ???????????????????????????
    private AddDialog addDialog;
    private DatePickerDialog.OnDateSetListener datePicker;
    private Calendar calendar;
    private Date date;

    // ??????????????????
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

    // ????????????????????????
    private DetailDialog detailDialog;

    private MapView mapViewInDetail;
    private BaiduMap baiduMapInDetail;

    LatLng targetPoint;


    private RequestOptions requestOptions = RequestOptions.circleCropTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)//??????????????????
            .skipMemoryCache(true);//??????????????????


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        fullScreenConfig();
        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_main);

        initPage();

        initHead();

        // ???????????????
        mMapView = findViewById(R.id.bd_map_view);
        mBaiduMap = mMapView.getMap();

        mMapView.showZoomControls(false);

        scheduleFirst = new Schedule();

        // ????????????
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

            // ??????????????????
            // android8.0???????????????NotificationUtils
            if (Build.VERSION.SDK_INT >= 26) {
                mNotificationUtils = new NotificationUtils(this);
                Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification
                        ("??????android 8????????????????????????", "??????????????????");
                notification = builder2.build();
            } else {
                // ????????????Notification?????????
                Notification.Builder builder = new Notification.Builder(MainActivity.this);
                Intent nfIntent = new Intent(MainActivity.this, MainActivity.class);

                builder.setContentIntent(PendingIntent.
                        getActivity(MainActivity.this, 0, nfIntent, 0)) // ??????PendingIntent
                        .setContentTitle("??????android????????????????????????")         // ??????????????????????????????
                        .setSmallIcon(R.drawable.logo)                      // ??????????????????????????????
                        .setContentText("??????????????????")                       // ?????????????????????
                        .setWhen(System.currentTimeMillis());               // ??????????????????????????????

                notification = builder.build(); // ??????????????????Notification
            }
            notification.defaults = Notification.DEFAULT_SOUND;             //????????????????????????
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

                    // ?????????????????? ???????????????????????????????????????????????? ??????????????? !!!!!

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

    // ?????????????????????????????????????????????
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
                        Log.d(TAG, "??????Url?????????" + e.toString() + "iii");
                    }
                });
    }

    // ???????????????????????? ???????????????????????????
    private void initPage() {

        setPopWindow();

        // ????????????
        loginImg = findViewById(R.id.go_to_login);
        OnClickHead onClick = new OnClickHead();
        loginImg.setOnClickListener(onClick);

        // ??????????????????
        checkTrip = findViewById(R.id.iv_check_trip);
        OnClickList onClickList = new OnClickList();
        checkTrip.setOnClickListener(onClickList);

        // ??????????????????
        newTrip = findViewById(R.id.iv_new_trip);
        OnClickLog onClickLog = new OnClickLog();
        newTrip.setOnClickListener(onClickLog);

        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initHead();

        initScheduleToShow();
    }

    // ??????????????????
    private void initHead() {
        String imageUrl = SPUtils.getString("imageUrl",null,MainActivity.this);
        if(imageUrl != null) {
            Glide.with(MainActivity.this).load(imageUrl).apply(requestOptions).into(loginImg);
        } else {
            loginImg.setImageResource(R.drawable.default_head);
        }
    }

    // ???????????????????????????---------????????????
    private class OnClickHead implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (sharedPreferences.getString("telephone", "") == "") {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    // ??????????????????
    private class OnClickList implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (sharedPreferences.getString("telephone", "") == "") {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
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
    // ========================================== ???????????? end ==========================================


    // ========================================== ???????????? begin ==========================================
    // ?????????????????????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "?????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    initLocation();
                    mLocationClient.start();

                    // ??????????????????
                    // android8.0???????????????NotificationUtils
                    if (Build.VERSION.SDK_INT >= 26) {
                        mNotificationUtils = new NotificationUtils(this);
                        Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification
                                ("??????android 8????????????????????????", "??????????????????");
                        notification = builder2.build();
                    } else {
                        // ????????????Notification?????????
                        Notification.Builder builder = new Notification.Builder(MainActivity.this);
                        Intent nfIntent = new Intent(MainActivity.this, MainActivity.class);

                        builder.setContentIntent(PendingIntent.
                                getActivity(MainActivity.this, 0, nfIntent, 0))  // ??????PendingIntent
                                .setContentTitle("??????android????????????????????????")                               // ??????????????????????????????
                                .setSmallIcon(R.drawable.logo)                                            // ??????????????????????????????
                                .setContentText("??????????????????")                                             // ?????????????????????
                                .setWhen(System.currentTimeMillis());                                     // ??????????????????????????????

                        notification = builder.build();                             // ??????????????????Notification
                    }
                    notification.defaults = Notification.DEFAULT_SOUND;             //????????????????????????
                } else {
                    Log.d(TAG, "??????????????????");
                    finish();
                }
                break;
        }
    }

    // ???????????????????????? ccc
    private void initLocation() {

        mBaiduMap.setMyLocationEnabled(true);

        // ???????????????
        mLocationClient = new LocationClient(getApplicationContext());

        // ??????????????????
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

    // ???????????????????????????????????? ccc
    private void initLocationInPick() {
        mapViewInPick = pickAddressDialog.mvInDialog;
        baiduMapInPick = mapViewInPick.getMap();

        fitAddressName = pickAddressDialog.moveName;
        fitAddressDesc = pickAddressDialog.moveAddress;

        gsonSaveLocation = new Gson();
        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

        // ??????????????????
        MapStatus mapStatusInPick = new MapStatus.Builder().zoom(18).build();
        MapStatusUpdate mapStatusUpdateInPick = MapStatusUpdateFactory.newMapStatus(mapStatusInPick);

        // ??????????????????
        baiduMapInPick.setMapStatus(mapStatusUpdateInPick);

        // ??????????????????????????????
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
                Log.d("to_save_data", "?????????" + pointLongitude + "  ?????????" + pointLatitude);
                // ?????????????????????????????????sharedPreferences???
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

        // ??????????????????
        baiduMapInPick.setMyLocationEnabled(true);
        // ????????????????????????
        mCurrentModeInPick = MyLocationConfiguration.LocationMode.NORMAL;
        // ??????????????????????????????
        baiduMapInPick.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentModeInPick, true, null));

        // ???????????????
        locationClientInPick = new LocationClient(getApplicationContext());
        // ??????????????????
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

    // ??????????????????
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

    // ??????????????????????????? ccc
    private void initLocationInDetail(double longitude, double latitude) {
        mapViewInDetail = detailDialog.detailAddressBaiduMap;
        baiduMapInDetail = mapViewInDetail.getMap();

        mapViewInDetail.showScaleControl(false);
        mapViewInDetail.showZoomControls(false);
        // ???????????????????????????????????????
        baiduMapInDetail.getUiSettings().setAllGesturesEnabled(false);

        baiduMapInDetail.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        mapViewInDetail.setLogoPosition(LogoPosition.logoPostionleftBottom);

        LatLng point = new LatLng(latitude, longitude);

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(point).zoom(19.5f);
        baiduMapInDetail.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        detailDialog.detailPoint.setVisibility(View.VISIBLE);

    }

    // ????????????????????????????????????????????? ccc
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

    // ?????????????????????????????? ccc
    private class MyLocationListenerInPick extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || baiduMapInPick == null) {
                return;
            }

            // ????????????
            MyLocationData data = new MyLocationData.Builder()
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();

            // ??????????????????
            baiduMapInPick.setMyLocationData(data);

            // ?????????????????????
            if (isFirstLocatedInPick) {
                isFirstLocatedInPick = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
                baiduMapInPick.animateMapStatus(msu);
            }

        }
    }
    // ========================================== ???????????? end ==========================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);    // ??????????????????
        mLocationClient.stop();                   // ??????????????????
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

    // ========================================== ?????????????????? end ==========================================

    private void initScheduleToShow() {

        gson = new Gson();

        asyncGetScheduleWithXHttp2(sharedPreferences.getString("telephone", ""));

        // ???sharedPreferences?????????????????????List
        jsonToGet = sharedPreferences.getString("schedule_list", "");
        Type type = new TypeToken<List<Schedule>>() {}.getType();
        scheduleList = gson.fromJson(jsonToGet, type);

        if (scheduleList != null) {
            getScheduleFirstItem();
        }


    }

    // ???????????????????????????????????????
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

            // ?????????????????????
            double dis = DistanceUtils.calculateDistance(targetPoint.latitude, targetPoint.longitude, currentPoint.latitude, currentPoint.longitude);
            Log.d("distance", dis + "--");

            // ????????????????????????????????????????????????????????? ????????????
            if (scheduleFirst.getScheduleStartTime() - timeString > 0 && scheduleFirst.getScheduleStartTime() - timeString <= 1000 * 60 * 10) {

                if (dis >= 300 && flag) {

                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent0 = new Intent(MainActivity.this, AlarmReceiver.class);
                    PendingIntent pendingIntent0 = PendingIntent.getBroadcast(MainActivity.this, 0, intent0, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent0);

                    // ????????????
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
                        NotificationChannel channel = new NotificationChannel("to-do", "????????????", NotificationManager.IMPORTANCE_HIGH);
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

                    // ????????????
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
                        NotificationChannel channel = new NotificationChannel("to-do", "????????????", NotificationManager.IMPORTANCE_HIGH);
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

    // ?????????????????????????????????????????????????????????
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

                        Log.d(TAG, "??????URL?????????" + data);
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
                        Log.d(TAG, "??????Url?????????" + e.toString());
                        editor.putString("schedule_first", gsonFirst.toJson((Object) null));
                        editor.commit();
                        Log.d(TAG, e.getMessage());
                    }
                });
    }


    /**
     * ????????????????????????????????????????????????
     * @param telephone
     */
    public void asyncGetScheduleWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetScheduleURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<List<Schedule>>() {
                    @Override
                    public void onSuccess(List<Schedule> data) {

                        Log.d(TAG, "??????URL?????????" + data);
                        if (data != null) {
                            // ???????????????List??????sharedPreferences???
                            editor.putString("schedule_list", gson.toJson(data));
                            if (editor.commit()) {
                                Log.d(TAG, "save success!!!");
                            } else {
                                Log.d(TAG, "save fail......");
                            }
                            setDateAndTimePickDialog();

                            gridView = contentView.findViewById(R.id.schedule_pop_list);
                            gridAdapter = new GridAdapter(MainActivity.this, data, onClickListener);  // ??????????????????
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

                                // ????????????????????????????????????dialog
                                detailDialog = new DetailDialog(MainActivity.this, R.style.dialog);

                                detailDialog.setOnClickListener(new DetailDialog.OnClickListener() {
                                    @Override
                                    public void updateTimeClick() {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);
                                        // ??????????????????????????????Dialog???

                                        isUpdatingTime = true;

                                        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                                AlertDialog.THEME_HOLO_DARK,
                                                datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                                        datePickerDialog.setTitle("????????????");
                                        datePickerDialog.show();
                                    }

                                    @Override
                                    public void updateAddressClick() {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(detailDialog.detailTimeStr.getWindowToken(), 0);

                                        isUpdatingAddress = true;

                                        // ???????????????POI?????????????????????Dialog????????????????????????
                                        pickAddressDialog = new PickAddressDialog(MainActivity.this, R.style.dialog_address);

                                        // ????????????????????????????????????????????????
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
                                        pickAddressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);  // ??????EditText ???????????????

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

                                        // ??????????????????
                                        String telephone1 = sharedPreferences.getString("telephone", "");
                                        // ???????????????????????????
                                        String inputTitle = detailDialog.detailTitle.getText().toString();
                                        // ???????????????????????????
                                        String inputDesc = detailDialog.detailDesc.getText().toString();

                                        int year = sharedPreferences.getInt("year", 2000);
                                        int month = sharedPreferences.getInt("month", 8);
                                        int day = sharedPreferences.getInt("day", 28);
                                        int hour = sharedPreferences.getInt("hour", 12);
                                        int minute = sharedPreferences.getInt("minute", 28);
                                        int second = 0;
                                        // ???????????????????????????
                                        date = new Date();
                                        date.setYear(year);
                                        date.setMonth(month);
                                        date.setDate(day);
                                        date.setHours(hour);
                                        date.setMinutes(minute);
                                        date.setSeconds(second);
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Long timeString = DateUtils.getString2Time(dateFormat.format(date), "yyyy-MM-dd HH:mm:ss");

                                        // ???sharedPreferences??????????????????????????????
                                        jsonLocationToGet = sharedPreferences.getString("location_save", "");
                                        Type type = new TypeToken<LatLng>() {}.getType();
                                        latLngGet = gson.fromJson(jsonLocationToGet, type);

                                        // ???????????????????????????
                                        BigDecimal longitudeToServer = BigDecimal.valueOf(latLngGet.longitude);
                                        // ???????????????????????????
                                        BigDecimal latitudeToServer = BigDecimal.valueOf(latLngGet.latitude);

                                        // ??????????????????????????????????????????
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
                                        // ????????????????????????
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
                        Log.d(TAG, "??????Url?????????" + e.toString());
                        editor.putString("schedule_first", gsonFirst.toJson((Object) null));
                        editor.commit();
                        gridView = contentView.findViewById(R.id.schedule_pop_list);
                        gridView.setAdapter(null);
//                        showToastInThread(MainActivity.this, e.getMessage());
                    }
                });
    }


    // ????????????????????? ??????????????????
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
     * ???????????????????????????
     * @param jsonStr
     */
    private void asyncAddScheduleWithXHttp2(final String jsonStr) {
        XHttp.post(NetConstant.getAddScheduleURL())
                .params("jsonStr", jsonStr)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) {
                        Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();

                        new Thread(() -> runOnUiThread(() -> fresh())).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "??????Url?????????" + e.toString());
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
                        Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();

                        new Thread(() -> runOnUiThread(() -> fresh())).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "??????Url?????????" + e.toString());
//                        showToastInThread(MainActivity.this, e.getMessage());
                    }
                });
    }


    /**
     * ???????????????????????????id????????????
     * @param id
     */
    private void asyncDeleteScheduleWithXHttp2(final Integer id) {
        XHttp.post(NetConstant.getDeleteScheduleURL())
                .params("scheduleId", id)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) {
                        Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();

                        new Thread(() -> runOnUiThread(() -> fresh())).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "??????Url?????????" + e.toString());
//                        showToastInThread(MainActivity.this, e.getMessage());
                    }
                });
    }


    // ??????????????????????????????????????????
    private class OnClickLog implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            setDateAndTimePickDialog();

            if (sharedPreferences.getString("telephone", "") == "") {
                Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                }
            } else {

                addDialog = new AddDialog(MainActivity.this, R.style.dialog);
                addDialog.setOnClickListener(new AddDialog.OnClickListener() {

                    /**
                     * ?????????????????????
                     */
                    @Override
                    public void onPickTimeClick() {

                        // ???????????????
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(addDialog.btPickTime.getWindowToken(), 0);

                        // ??????????????????????????????Dialog???
                        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                AlertDialog.THEME_HOLO_DARK,
                                datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.setTitle("???????????????");
                        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "??????", (dialog, which) ->
                                Toast.makeText(MainActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show());
                        datePickerDialog.show();
                    }

                    /**
                     * ????????????
                     */
                    @Override
                    public void onPickAddressClick() {

                        // ???????????????
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(addDialog.btPickAddress.getWindowToken(), 0);

                        // ???????????????POI?????????????????????Dialog????????????????????????
                        pickAddressDialog = new PickAddressDialog(MainActivity.this, R.style.dialog_address);

                        // ????????????????????????????????????????????????
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
                                Toast.makeText(MainActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
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
                        pickAddressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);  // ??????EditText ???????????????

                        initLocationInPick();
                        locationClientInPick.start();
                        isFirstLocatedInPick = true;

                    }

                    /**
                     * ???????????????
                     */
                    @Override
                    public void onSubmitClick() {

                        // ??????????????????
                        String telephone = sharedPreferences.getString("telephone", "");

                        // ???????????????????????????
                        String inputTitle = addDialog.edtTitle.getText().toString();
                        // ???????????????????????????
                        String inputDesc = addDialog.edtDesc.getText().toString();

                        // ???????????????????????????
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

                        // ???sharedPreferences??????????????????????????????
                        jsonLocationToGet = sharedPreferences.getString("location_save", "");
                        Type type = new TypeToken<LatLng>() {}.getType();
                        latLngGet = gson.fromJson(jsonLocationToGet, type);
                        // ???????????????????????????
                        BigDecimal longitudeToServer = BigDecimal.valueOf(latLngGet.longitude);
                        // ???????????????????????????
                        BigDecimal latitudeToServer = BigDecimal.valueOf(latLngGet.latitude);

                        // ??????????????????????????????????????????
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
                            Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
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
                addDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);  // ??????EditText ???????????????
            }
        }
    }

    /**
     * ????????????????????????????????????
     */
    public void setDateAndTimePickDialog() {
        calendar = Calendar.getInstance();
        datePicker = (view, year, month, dayOfMonth) -> {

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // ??????????????????sharedPreferences???
            editor.putInt("year", year - 1900);
            editor.putInt("month", month);
            editor.putInt("day", dayOfMonth);
            editor.commit();

            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                    TimePickerDialog.THEME_HOLO_DARK,
                    (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        // ??????????????????sharedPreferences???
                        editor.putInt("hour", hourOfDay);
                        editor.putInt("minute", minute);
                        editor.commit();
                        isGetTime = true;

                        // ???????????????????????????
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
            timePickerDialog.setTitle("?????????????????????");

            timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "??????", (dialog, which) -> {
                isUpdatingTime = false;
                Toast.makeText(MainActivity.this, "??????????????????????????????????????????", Toast.LENGTH_SHORT).show();
            });
            timePickerDialog.show();

        };

    }
}