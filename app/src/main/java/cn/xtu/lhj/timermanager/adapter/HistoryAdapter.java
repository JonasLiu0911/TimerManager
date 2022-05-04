package cn.xtu.lhj.timermanager.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.List;

import cn.xtu.lhj.timermanager.R;
import cn.xtu.lhj.timermanager.bean.Schedule;
import cn.xtu.lhj.timermanager.utils.BDMapUtils;
import cn.xtu.lhj.timermanager.utils.DateUtils;

public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private List<Schedule> scheduleList;
    private View.OnClickListener onClickListener;

    private boolean isShowDelete;

    public HistoryAdapter(Context context, List<Schedule> scheduleList, View.OnClickListener onClickListener) {
        this.context = context;
        this.scheduleList = scheduleList;
        this.onClickListener = onClickListener;
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
    public Schedule getItem(int position) {
        return scheduleList.get(position);
    }

    // 返回子项下标
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setIsShowDelete(boolean isShowDelete) {
        this.isShowDelete = isShowDelete;
        notifyDataSetChanged();
    }

    public boolean getIsShowDelete() {
        return isShowDelete;
    }

    // 返回子项视图
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Schedule schedule = (Schedule) getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_in_history, null);
            viewHolder = new ViewHolder();
            viewHolder.tvScheduleTitle = view.findViewById(R.id.tv_schedule_title);
            viewHolder.tvScheduleInfo = view.findViewById(R.id.tv_schedule_info);
            viewHolder.tvScheduleStartTime = view.findViewById(R.id.tv_start_time);
            viewHolder.tvScheduleLocation = view.findViewById(R.id.tv_location);
            viewHolder.ivDeleteSchedule = view.findViewById(R.id.iv_delete_item);     // 删除按钮（图片）
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        String scheduleTitle = schedule.getScheduleTitle();
        viewHolder.tvScheduleTitle.setText(cut(scheduleTitle, 9));

        String scheduleInfo = schedule.getScheduleInfo();
        viewHolder.tvScheduleInfo.setText(cut(scheduleInfo, 16));

        long scheduleTimeString = schedule.getScheduleStartTime();
        String scheduleStartTime = DateUtils.getTime2String(scheduleTimeString, "yyyy-MM-dd HH:mm");
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
                    viewHolder.tvScheduleLocation.setText(reverseGeoCodeResult.getSematicDescription());
                }
            }
        });

        viewHolder.ivDeleteSchedule.bringToFront();
        viewHolder.ivDeleteSchedule.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);
        viewHolder.ivDeleteSchedule.setTag(position);
        viewHolder.ivDeleteSchedule.setOnClickListener(this.onClickListener);

        return view;
    }

    // 创建ViewHolder类
    class ViewHolder {
        TextView tvScheduleTitle;
        TextView tvScheduleInfo;
        TextView tvScheduleStartTime;
        TextView tvScheduleLocation;
        ImageView ivDeleteSchedule;
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
