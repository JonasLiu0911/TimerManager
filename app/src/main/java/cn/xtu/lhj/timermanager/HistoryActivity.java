package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import java.util.List;

import cn.xtu.lhj.timermanager.adapter.GridAdapter;
import cn.xtu.lhj.timermanager.adapter.HistoryAdapter;
import cn.xtu.lhj.timermanager.bean.Schedule;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.dialogs.HistoryDialog;
import cn.xtu.lhj.timermanager.utils.DateUtils;

public class HistoryActivity extends BaseActivity {

    private final String TAG = "HistoryActivity";

    ActionBar actionBar;

    private GridView gridView;
    private HistoryAdapter historyAdapter;
    private boolean isShowDelete = false;
    private Gson gson;
    private String jsonSaveHistory;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private HistoryDialog historyDialog;
    private MapView mapViewInHistory;
    private BaiduMap baiduMapInHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("????????????");
        }

        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initScheduleToShow();
    }

    private void initLocationInHistory(double longitude, double latitude) {
        mapViewInHistory = historyDialog.historyAddress;
        baiduMapInHistory = mapViewInHistory.getMap();

        mapViewInHistory.showZoomControls(false);
        mapViewInHistory.showScaleControl(false);
        baiduMapInHistory.getUiSettings().setAllGesturesEnabled(false);
        baiduMapInHistory.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mapViewInHistory.setLogoPosition(LogoPosition.logoPostionleftBottom);

        LatLng point = new LatLng(latitude, longitude);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(point).zoom(19.5f);
        baiduMapInHistory.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    private void initScheduleToShow() {

        gson = new Gson();
        asyncGetHistoryWithXHttp2(sharedPreferences.getString("telephone", ""));

    }

    public void asyncGetHistoryWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetHistoryURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<List<Schedule>>() {
                    @Override
                    public void onSuccess(List<Schedule> data) {
                        Log.d(TAG, "??????URL?????????" + data);

                        if (data != null) {
                            // ???????????????List??????sharedPreferences???
                            jsonSaveHistory = gson.toJson(data);
                            editor.putString("history_list", jsonSaveHistory);

                            if (editor.commit()) {
                                Log.d(TAG, "save success!!!");
                            } else {
                                Log.d(TAG, "save fail......");
                            }

                            gridView = findViewById(R.id.grid_view_history);
                            historyAdapter = new HistoryAdapter(HistoryActivity.this, data, onClickListener);
                            gridView.setAdapter(historyAdapter);

                            gridView.setOnItemLongClickListener((parent, view, position, id) -> {
                                if (historyAdapter.getIsShowDelete()) {
                                    isShowDelete = false;
                                } else {
                                    isShowDelete = true;
                                }
                                historyAdapter.setIsShowDelete(isShowDelete);

                                return true;
                            });

                            gridView.setOnItemClickListener((parent, view, position, id) -> {

                                historyAdapter.setIsShowDelete(false);
                                Schedule scheduleHistory = historyAdapter.getItem(position);

                                historyDialog = new HistoryDialog(HistoryActivity.this, R.style.dialog);
                                historyDialog.show();
                                historyDialog.historyTitle.setText(scheduleHistory.getScheduleTitle());
                                historyDialog.historyDesc.setText(scheduleHistory.getScheduleInfo());
                                historyDialog.historyTimeStr.setText(DateUtils.getTime2String(scheduleHistory.getScheduleStartTime(), "yyyy-MM-dd HH:mm"));
                                initLocationInHistory(scheduleHistory.getLongitude().doubleValue(), scheduleHistory.getLatitude().doubleValue());
                            });

                        } else {
                            Toast.makeText(HistoryActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "??????Url?????????" + e.toString());
//                        showToastInThread(HistoryActivity.this, e.getMessage());
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
                        Toast.makeText(HistoryActivity.this, "????????????", Toast.LENGTH_SHORT).show();

                        new Thread(() -> runOnUiThread(() -> fresh())).start();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "??????Url?????????" + e.toString());
//                        showToastInThread(HistoryActivity.this, e.getMessage());
                    }
                });
    }

    // ????????????????????? ??????????????????
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView deleteItem = (ImageView) v;
            int pos = (Integer) deleteItem.getTag();
            Schedule scheduleToDelete = historyAdapter.getItem(pos);
            asyncDeleteScheduleWithXHttp2(scheduleToDelete.getId());

        }
    };

    private void fresh() {
        try {
            Thread.sleep(1000);
            initScheduleToShow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ??????????????????
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}