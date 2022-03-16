package cn.xtu.lhj.timermanager.application;

import android.app.Application;

import com.xuexiang.xhttp2.XHttpSDK;

import cn.xtu.lhj.timermanager.constant.NetConstant;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        XHttpSDK.init(this);             //初始化网络请求框架
        XHttpSDK.setBaseUrl(NetConstant.baseUrl);  //设置网络请求基地址
    }

}

/**
 * Android Studio Terminal连接MuMu模拟器
 *
 * cd D:\android-sdk_r24.4.1-windows\android-sdk-windows\platform-tools
 * adb connect 127.0.0.1:7555
 */
