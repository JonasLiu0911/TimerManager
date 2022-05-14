package cn.xtu.lhj.timermanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

import cn.xtu.lhj.timermanager.R;
import cn.xtu.lhj.timermanager.bean.Result;
import cn.xtu.lhj.timermanager.utils.DateUtils;

public class AnalyzeAdapter extends BaseAdapter {

    private Context context;
    private List<String> keyList;

    public AnalyzeAdapter(Context context, List<String> keyList) {
        this.context = context;
        this.keyList = keyList;
    }

    @Override
    public int getCount() {
        if (keyList == null) {
            return 0;
        }
        return keyList.size();
    }

    @Override
    public Object getItem(int position) {
        return keyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String key = (String) getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_in_analyze, null);
            viewHolder = new ViewHolder();
            viewHolder.tvAnalyzeDate = view.findViewById(R.id.tv_analyze_date);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvAnalyzeDate.setText(key);

        return view;
    }

    class ViewHolder {
        TextView tvAnalyzeDate;
    }
}
