package cn.xtu.lhj.timermanager.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.baidu.mapapi.map.MapView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.xtu.lhj.timermanager.R;
import cn.xtu.lhj.timermanager.bean.Schedule;

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
//            viewHolder.mvScheduleLocation = view.findViewById(R.id.mv_schedule_location);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        String scheduleTitle = schedule.getScheduleTitle();
        viewHolder.tvScheduleTitle.setText(cut(scheduleTitle, 5));

        String scheduleInfo = schedule.getScheduleInfo();
        viewHolder.tvScheduleInfo.setText(cut(scheduleInfo, 24));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date scheduleStartTime = schedule.getScheduleStartTime();
        viewHolder.tvScheduleStartTime.setText(dateFormat.format(scheduleStartTime));

        return view;
    }

    // 创建ViewHolder类
    class ViewHolder {
        TextView tvScheduleTitle;
        TextView tvScheduleInfo;
        TextView tvScheduleStartTime;
        MapView mvScheduleLocation;
    }

    // 长度裁剪
    private String cut(String content, int length) {
        int contentLength = content.getBytes().length / 3;

        if (contentLength < length) {
            return content;
        } else {
            String result = content.substring(0, length - 1) + "...";
            return result;
        }
    }
}
