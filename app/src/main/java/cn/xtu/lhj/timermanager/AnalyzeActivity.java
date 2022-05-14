package cn.xtu.lhj.timermanager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.xtu.lhj.timermanager.adapter.AnalyzeAdapter;
import cn.xtu.lhj.timermanager.adapter.DialogGridAdapter;
import cn.xtu.lhj.timermanager.bean.Result;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.dialogs.AnalyzeDialog;
import cn.xtu.lhj.timermanager.dialogs.PieChartDialog;
import cn.xtu.lhj.timermanager.utils.DateUtils;
import cn.xtu.lhj.timermanager.view.PieChartView;

public class AnalyzeActivity extends BaseActivity {

    private final String TAG = "AnalyzeActivity";

    ActionBar actionBar;

    private GridView gridView;
    private AnalyzeAdapter analyzeAdapter;

    private Map<String, List<Result>> listMap;

    private Gson gson;
    private SharedPreferences sharedPreferences;

    private AnalyzeDialog analyzeDialog;
    private DialogGridAdapter dialogGridAdapter;

    private PieChartDialog pieChartDialog;

    // rrrrrr
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("行程分析");
        }

        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

        initAnalyzeToShow();
    }

    private void initAnalyzeToShow() {

        gson = new Gson();
        String telephone = sharedPreferences.getString("telephone", "");
        Log.d("resultsdd", telephone);
        asyncGetAnalyzeWithXHttp2(telephone);

    }

    public void asyncGetAnalyzeWithXHttp2(String telephone) {
        XHttp.post(NetConstant.getGetResultsByTelURL())
                .params("telephone", telephone)
                .syncRequest(false)
                .execute(new SimpleCallBack<List<Result>>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(List<Result> data) throws Throwable {
                        Log.d(TAG, "请求URL成功：" + data);
                        listMap = new HashMap<>();
                        List<Result> results = null;

                        if (data != null) {
                            Log.d("resultsdd", "not null");
                            // 处理从后端获取的分析结果数据data
                            // 遍历，按照 格式化日期 进行分组，存入map中
                            for (Result i : data) {
                                String yyyyMMdd = DateUtils.getTime2String(i.getBeginTime(), "yyyy-MM-dd");
                                Log.d("results", yyyyMMdd);
                                if (listMap.get(yyyyMMdd) == null) {
                                    results = new ArrayList<>();
                                }

                                results.add(i);
                                listMap.put(yyyyMMdd, results);
                            }

                            List<String> dateList = new ArrayList<>();
                            for (String key : listMap.keySet()) {
                                dateList.add(key);
                            }

                            Collections.sort(dateList);
                            Collections.reverse(dateList);

                            gridView = findViewById(R.id.gv_analyze);
                            analyzeAdapter = new AnalyzeAdapter(AnalyzeActivity.this, dateList);
                            gridView.setAdapter(analyzeAdapter);

                            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                                    Map<String, Float> floatMap = new HashMap<>();
                                    float times = 0;

                                    // 把时间存到map里面，去画扇形图
                                    String analDate = (String) analyzeAdapter.getItem(position);
                                    List<Result> resultToDraw = listMap.get(analDate);
                                    for (Result i : resultToDraw) {
                                        Log.d("sysy", i.getResultTag() + " " + (float) (i.getFinishTime() - i.getBeginTime()) / 60000);
                                        String tag = i.getResultTag();
                                        if (floatMap.get(tag) == null) {
                                            times = 0.0f;
                                        }
                                        times += (float) (i.getFinishTime() - i.getBeginTime()) / 60000;
                                        floatMap.put(tag, times);
                                    }

                                    for (String t : floatMap.keySet()) {
                                        Log.d("sysy--", t + " " + floatMap.get(t));
                                    }

                                    pieChartDialog = new PieChartDialog(AnalyzeActivity.this, R.style.dialog);
                                    pieChartDialog.show();
                                    pieChartDialog.pieDate.setText(analDate);


                                    ArrayList<PieChartView.Part> list = new ArrayList<>();

                                    for (String t : floatMap.keySet()) {
                                        list.add(new PieChartView.Part(t + "  " + Math.round(floatMap.get(t)) + " min", floatMap.get(t), getColor()));
                                    }

                                    pieChartDialog.pieChartView.setPartsData(list);

                                    return true;
                                }
                            });

                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String analDate = (String) analyzeAdapter.getItem(position);

                                    List<Result> resultToShow = listMap.get(analDate);
                                    analyzeDialog = new AnalyzeDialog(AnalyzeActivity.this, R.style.dialog);
                                    analyzeDialog.show();
                                    analyzeDialog.analyzeDate.setText(analDate);

                                    // 设置分析弹窗内的行程列表
                                    dialogGridAdapter = new DialogGridAdapter(AnalyzeActivity.this, resultToShow);
                                    analyzeDialog.gvLists.setAdapter(dialogGridAdapter);
                                }
                            });
                        } else {
                            Toast.makeText(AnalyzeActivity.this, "暂无行程分析", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
                    }
                });
    }


    // 生成随机颜色
    public int getColor() {
        // 红色
        String red;
        // 绿色
        String green;
        // 蓝色
        String blue;
        // 生成随机对象
        Random random = new Random();
        // 生成红色颜色代码
        red = Integer.toHexString(random.nextInt(256)).toUpperCase();
        // 生成绿色颜色代码
        green = Integer.toHexString(random.nextInt(256)).toUpperCase();
        // 生成蓝色颜色代码
        blue = Integer.toHexString(random.nextInt(256)).toUpperCase();

        // 判断红色代码的位数
        red = red.length()==1 ? "0" + red : red ;
        // 判断绿色代码的位数
        green = green.length()==1 ? "0" + green : green ;
        // 判断蓝色代码的位数
        blue = blue.length()==1 ? "0" + blue : blue ;
        // 生成十六进制颜色值
        String color = "#" + red + green + blue;
        return Color.parseColor(color);
    }

    // 菜单点击实现
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