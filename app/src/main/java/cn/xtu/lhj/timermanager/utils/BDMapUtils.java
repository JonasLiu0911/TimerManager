package cn.xtu.lhj.timermanager.utils;

import android.location.Geocoder;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;

public class BDMapUtils {

    static GeoCoder geoCoder = GeoCoder.newInstance();

    // 将经纬度反译为地址文字信息
    public static void reverseGeoParse(Double longitude, Double latitude, OnGetGeoCoderResultListener listener) {
        geoCoder.setOnGetGeoCodeResultListener(listener);
        LatLng latLng = new LatLng(latitude, longitude);
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
    }
}
