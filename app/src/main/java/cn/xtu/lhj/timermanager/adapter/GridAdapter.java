package cn.xtu.lhj.timermanager.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.xtu.lhj.timermanager.MainActivity;
import cn.xtu.lhj.timermanager.R;
import cn.xtu.lhj.timermanager.bean.Schedule;
import cn.xtu.lhj.timermanager.utils.BDMapUtils;
import cn.xtu.lhj.timermanager.utils.DateUtils;

public class GridAdapter extends BaseAdapter {

    private Context context;
    private List<Schedule> scheduleList;

    public GridAdapter(Context context, List<Schedule> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;
    }

    // 返回子项个数
    @Override
    public int getCount() {
        if (scheduleList == null) {
            return 0;
        }
        return scheduleList.size();
    }

    // 返回子项对应的对象
    @Override
    public Object getItem(int position) {
        return scheduleList.get(position);
    }

    // 返回子项下标
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 返回子项视图
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Schedule schedule = (Schedule) getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_gridview, null);
            viewHolder = new ViewHolder();

            viewHolder.tvScheduleTitle = view.findViewById(R.id.tv_schedule_title);
            viewHolder.tvScheduleInfo = view.findViewById(R.id.tv_schedule_info);
            viewHolder.tvScheduleStartTime = view.findViewById(R.id.tv_start_time);
            viewHolder.tvScheduleLocation = view.findViewById(R.id.tv_location);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        String scheduleTitle = schedule.getScheduleTitle();
        viewHolder.tvScheduleTitle.setText(cut(scheduleTitle, 8));

        String scheduleInfo = schedule.getScheduleInfo();
        viewHolder.tvScheduleInfo.setText(cut(scheduleInfo, 16));

        long scheduleTimeString = schedule.getScheduleStartTime();
        String scheduleStartTime = DateUtils.getTime2String(scheduleTimeString, "yyyy-MM-dd HH:mm");
        Log.d("check", schedule.getScheduleStartTime().toString());
        viewHolder.tvScheduleStartTime.setText(scheduleStartTime);

        Double scheduleLongitude = (Double) schedule.getLongitude().doubleValue();
        Double scheduleLatitude = (Double) schedule.getLatitude().doubleValue();

        BDMapUtils.reverseGeoParse(scheduleLongitude, scheduleLatitude, new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    viewHolder.tvScheduleLocation.setText("未找到搜索结果");
                } else {
                    viewHolder.tvScheduleLocation.setText(cut(reverseGeoCodeResult.getSematicDescription(), 18));
                }
            }
        });
        Log.d("Longitude Latitude", scheduleLongitude + " " + scheduleLatitude);

        return view;
    }

    // 创建ViewHolder类
    class ViewHolder {
        TextView tvScheduleTitle;
        TextView tvScheduleInfo;
        TextView tvScheduleStartTime;
        TextView tvScheduleLocation;
    }

    // 长度裁剪
    private String cut(String content, int length) {
        int contentLength = content.getBytes().length / 3;

        if (contentLength < length) {
            return content;
        } else {
            String result = content.substring(0, length - 1) + "......";
            return result;
        }
    }
}
