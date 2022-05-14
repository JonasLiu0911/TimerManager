package cn.xtu.lhj.timermanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.xtu.lhj.timermanager.R;
import cn.xtu.lhj.timermanager.bean.Result;
import cn.xtu.lhj.timermanager.utils.DateUtils;

public class DialogGridAdapter extends BaseAdapter {

    private Context context;
    private List<Result> resultList;

    public DialogGridAdapter(Context context, List<Result> resultList) {
        this.context = context;
        this.resultList = resultList;
    }

    @Override
    public int getCount() {
        if (resultList == null) {
            return 0;
        }
        return resultList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Result result = (Result) getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_in_dialog, null);
            viewHolder = new ViewHolder();
            viewHolder.beginTime = view.findViewById(R.id.tv_begin_time);
            viewHolder.finishTime = view.findViewById(R.id.tv_finish_time);
            viewHolder.locDesc = view.findViewById(R.id.tv_loc_desc);
            viewHolder.behTag = view.findViewById(R.id.tv_beh_tag);
            viewHolder.reItemDialog = view.findViewById(R.id.rl_item_dia);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.beginTime.setText(DateUtils.getTime2String(result.getBeginTime(), "HH:mm"));
        viewHolder.finishTime.setText(DateUtils.getTime2String(result.getFinishTime(), "HH:mm"));
        viewHolder.locDesc.setText(result.getResultDesc());
        viewHolder.behTag.setText(result.getResultTag());

        return view;
    }

    class ViewHolder {
        TextView beginTime;
        TextView finishTime;
        TextView locDesc;
        TextView behTag;
        RelativeLayout reItemDialog;
    }
}
