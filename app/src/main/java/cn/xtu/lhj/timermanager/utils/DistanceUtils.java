package cn.xtu.lhj.timermanager.utils;

public class DistanceUtils {

    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // 转弧度
        lat1 = lat1 * Math.PI / 180;
        lng1 = lng1 * Math.PI / 180;
        lat2 = lat2 * Math.PI / 180;
        lng2 = lng2 * Math.PI / 180;

        double cos = Math.cos(lat2) * Math.cos(lat1) * Math.cos(lng2 - lng1) + Math.sin(lat1) * Math.sin(lat2);
        return Math.acos(cos) * 6370996.81;
    }

    // 效果不行
    public static double distHaversineRAD(double lat1, double lng1, double lat2, double lng2) {
        double hsinX = Math.sin((lng1 - lng2) * 0.5);
        double hsinY = Math.sin((lat1 - lat2) * 0.5);
        double h = hsinY * hsinY +
                (Math.cos(lat1) * Math.cos(lat2) * hsinX * hsinX);
        return 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h)) * 6367000;
    }

    public static double distanceSimplify(double lat1, double lng1, double lat2, double lng2) {
        double dx = lng1 - lng2;          // 经度差
        double dy = lat1 - lat2;          // 纬度差
        double b = (lat1 + lat2) / 2.0;   // 平均纬度
        double Lx = Math.toRadians(dx) * 6367000.0 * Math.cos(Math.toRadians(b));   // 东西距离
        double Ly = 6370996.81 * Math.toRadians(dy);                                 // 南北距离
        return Math.sqrt(Lx * Lx + Ly * Ly);                                        // 用平面的矩形对角距离公式计算总距离
    }

}
