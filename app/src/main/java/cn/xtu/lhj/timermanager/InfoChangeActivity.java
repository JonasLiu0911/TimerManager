package cn.xtu.lhj.timermanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.Slider;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.callback.SimpleCallBack;
import com.xuexiang.xhttp2.exception.ApiException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import cn.xtu.lhj.timermanager.bean.UserInfo;
import cn.xtu.lhj.timermanager.constant.NetConstant;
import cn.xtu.lhj.timermanager.utils.BitmapUtils;
import cn.xtu.lhj.timermanager.utils.CameraUtils;
import cn.xtu.lhj.timermanager.utils.SPUtils;

public class InfoChangeActivity extends BaseActivity {

    private final String TAG = "InfoChangeActivity";

    //SharedPreferences对象
    SharedPreferences sharedPreferences;

    ActionBar actionBar;
    ShapeableImageView headToFill;
    TextView nickNameToFill;
    TextView genderToFill;
    TextView ageToFill;

    // 修改头像相关
    //权限请求
    private RxPermissions rxPermissions;

    //是否拥有权限
    private boolean hasPermissions = false;

    //底部弹窗
    private BottomSheetDialog bottomSheetDialog;
    //弹窗视图
    private View bottomView;

    //启动相册标识
    public static final int SELECT_PHOTO = 1;

    //Base64
    private String base64Pic;
    //拍照和相册获取图片的Bitmap
    private Bitmap orc_bitmap;

    private RequestOptions requestOptions = RequestOptions.circleCropTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)   //不做磁盘缓存
            .skipMemoryCache(true);                      //不做内存缓存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_change);

        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);

        initPage();

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("个人资料");
        }
    }

    public void initPage() {
        headToFill = findViewById(R.id.change_head);
        nickNameToFill = findViewById(R.id.change_et_nickname);
        genderToFill = findViewById(R.id.change_et_gender);
        ageToFill = findViewById(R.id.change_et_age);

        nickNameToFill.setText(sharedPreferences.getString("name", ""));
        genderToFill.setText(sharedPreferences.getString("gender", ""));
        ageToFill.setText(sharedPreferences.getString("age", ""));

        checkVersion();

        initHead();
    }

    public void initHead() {
        String imageUrl = SPUtils.getString("imageUrl",null,InfoChangeActivity.this);
        if(imageUrl != null){
            Glide.with(InfoChangeActivity.this).load(imageUrl).apply(requestOptions).into(headToFill);
        } else {
            headToFill.setImageResource(R.drawable.default_head);
        }
    }

    private void asyncUpdateHeadWithXHttp2(String telephone, String headUrl) {

        if (headUrl == null) {
            return;
        }

        XHttp.post(NetConstant.getUpdateHeadURL())
                .params("telephone", telephone)
                .params("headUrl", headUrl)
                .syncRequest(false)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onSuccess(Object response) throws Throwable {
                        Toast.makeText(InfoChangeActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ApiException e) {
                        Log.d(TAG, "请求Url异常：" + e.toString());
//                        showToastInThread(InfoChangeActivity.this, e.getMessage());
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void checkVersion() {
        // Android6.0及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            rxPermissions = new RxPermissions(this);
            //权限请求
            rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted) {                            //申请成功
                            Log.d(TAG, "已获取权限");
                            hasPermissions = true;
                        } else {                                  //申请失败
                            showMsg("权限未开启");
                            hasPermissions = false;
                        }
                    });
        } else {
            // Android6.0以下
            Log.d(TAG, "无需请求动态权限");
        }
    }

    public void toChangeHead(View view) {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundColor(Color.TRANSPARENT);
        TextView tvOpenAlbum = bottomView.findViewById(R.id.tv_open_album);
        TextView tvCancel = bottomView.findViewById(R.id.tv_cancel);

        //打开相册
        tvOpenAlbum.setOnClickListener(v -> {
            openAlbum();
            showMsg("打开相册");
            bottomSheetDialog.cancel();
        });
        //取消
        tvCancel.setOnClickListener(v -> bottomSheetDialog.cancel());
        //底部弹窗显示
        bottomSheetDialog.show();
    }

    private void openAlbum() {
        if (!hasPermissions) {
            showMsg("未获取到权限");
            checkVersion();
            return;
        }
        startActivityForResult(CameraUtils.getSelectPhotoIntent(), SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 打开相册后返回
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imagePath;
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        // 4.4及以上系统使用这个方法处理图片
                        imagePath = CameraUtils.getImageOnKitKatPath(data, this);
                    } else {
                        imagePath = CameraUtils.getImageBeforeKitKatPath(data, this);
                    }
                    // 显示图片
                    displayImage(imagePath);
                }
                break;
            default:
                break;
        }
    }

    private void displayImage(String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {

            String telephone = sharedPreferences.getString("telephone", "");
            SPUtils.putString("imageUrl", imagePath, this);

            asyncUpdateHeadWithXHttp2(telephone, imagePath);

            // 显示图片
            Glide.with(this).load(imagePath).apply(requestOptions).into(headToFill);

            // 压缩图片
            orc_bitmap = CameraUtils.compression(BitmapFactory.decodeFile(imagePath));
            // 转Base64
            base64Pic = BitmapUtils.bitmapToBase64(orc_bitmap);

        } else {
            showMsg("图片获取失败");
        }
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void toChangeName(View view) {
        Intent intent = new Intent(InfoChangeActivity.this, NameChangeActivity.class);
        startActivity(intent);
    }

    public void toChangeGender(View view) {
        Intent intent = new Intent(InfoChangeActivity.this, GenderChangeActivity.class);
        startActivity(intent);
    }

    public void toChangeAge(View view) {
        Intent intent = new Intent(InfoChangeActivity.this, AgeChangeActivity.class);
        startActivity(intent);
    }

    public void toChangePwd(View view) {
        Intent intent = new Intent(InfoChangeActivity.this, PwdChangeActivity.class);
        startActivity(intent);
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

    @Override
    public void onResume() {
        super.onResume();
        initPage();
    }
}