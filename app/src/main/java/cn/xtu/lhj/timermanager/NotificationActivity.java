package cn.xtu.lhj.timermanager;


import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.xuexiang.xhttp2.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.xtu.lhj.timermanager.bean.Schedule;
import cn.xtu.lhj.timermanager.utils.DateUtils;
import cn.xtu.lhj.timermanager.utils.DistanceUtils;

public class NotificationActivity extends BaseActivity {

    private final String TAG = "NotificationActivity";

    private MapView hereMapView;
    private BaiduMap hereBaiduMap;
    private LocationClient hereLocationClient;

    private ImageView herePoint;

    private TextView titleView;
    private TextView timeView;
    private TextView distanceView;

    private TextView hadArrived;
    private TextView notArrive;

    private Boolean flag = true;

    private SharedPreferences sharedPreferences;
    private String json;
    private Gson gson;
    private List<Schedule> scheduleList;
    private Schedule schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fullScreenConfig();
        getSupportActionBar().hide();

        setContentView(R.layout.activity_notification);

        gson = new Gson();
        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

        initPage();

    }

    private void initPage() {

        titleView = findViewById(R.id.here_title);
        timeView = findViewById(R.id.here_time_down);
        distanceView = findViewById(R.id.here_dis_down);
        hadArrived = findViewById(R.id.had_arrived);
        notArrive = findViewById(R.id.not_arrive);
        herePoint = findViewById(R.id.here_center_point);

        hadArrived.setVisibility(View.INVISIBLE);
        notArrive.setVisibility(View.INVISIBLE);
        setFlickerAnimation(herePoint);

        json = sharedPreferences.getString("schedule_first", "");
        Type type = new TypeToken<List<Schedule>>() {}.getType();
        scheduleList = gson.fromJson(json, type);
        schedule = scheduleList.get(0);
        initLocation(schedule.getLongitude().doubleValue(), schedule.getLatitude().doubleValue());
        hereLocationClient.start();

        titleView.setText(schedule.getScheduleTitle());

    }

    private void initLocation(double longitude, double latitude) {
        hereMapView = findViewById(R.id.here_map);
        hereBaiduMap = hereMapView.getMap();
        hereMapView.showZoomControls(false);
        hereMapView.showScaleControl(false);
        hereBaiduMap.getUiSettings().setAllGesturesEnabled(false);
        hereBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        hereMapView.setLogoPosition(LogoPosition.logoPostionleftBottom);

        LatLng point = new LatLng(latitude, longitude);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(point).zoom(18.5f);

        hereBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        hereBaiduMap.setMyLocationEnabled(true);
        hereLocationClient = new LocationClient(getApplicationContext());
        hereLocationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("BD09LL");
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setScanSpan(1000);
        hereLocationClient.setLocOption(option);
    }

    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || hereMapView == null) {
                return;
            }

            MyLocationData locationData = new MyLocationData.Builder()
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();

            hereBaiduMap.setMyLocationData(locationData);

            monitorLoc(bdLocation.getLongitude(), bdLocation.getLatitude());
        }
    }

    private void monitorLoc(double longitudeNow, double latitudeNow) {
        // 当前时间
        Date timeNow = new Date(System.currentTimeMillis());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

        Long timeStrNow = DateUtils.getString2Time(dateFormat.format(timeNow), "yyyy-MM-dd HH:mm:ss");
        Long timeStrSchedule = schedule.getScheduleStartTime();

        double disNow = DistanceUtils.calculateDistance(schedule.getLatitude().doubleValue(), schedule.getLongitude().doubleValue(),
                latitudeNow, longitudeNow);

        if (timeStrSchedule - timeStrNow > 0) {

            distanceView.setText((int) disNow + " m");
            timeView.setText(DateUtils.getTime2String(timeStrSchedule - timeStrNow, "mm:ss"));

            if (disNow <= 100 && flag) {
                hadArrived.setVisibility(View.VISIBLE);
                Toast.makeText(NotificationActivity.this, "已到达日程点附近", Toast.LENGTH_LONG).show();
                flag = false;
            }

        } else {
            if (disNow > 100) {
                Toast.makeText(NotificationActivity.this, "您仍未到达日程点，请抓紧时间", Toast.LENGTH_LONG).show();
            }
        }

    }

    // 实现图片闪烁效果
    private void setFlickerAnimation(ImageView iv_chat_head) {
        final android.view.animation.Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(android.view.animation.Animation.INFINITE);
        animation.setRepeatMode(android.view.animation.Animation.REVERSE);
        iv_chat_head.setAnimation(animation);
    }

    public void noHere(View view) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(0);
        Toast.makeText(NotificationActivity.this, "未到达", Toast.LENGTH_SHORT).show();
    }

    public void yesHere(View view) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(1);
        finish();
    }
}