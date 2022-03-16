package cn.xtu.lhj.timermanager.constant;

public class NetConstant {

    public static final String baseUrl = "http://498ua00061.qicp.vip";

    private static final String getOtpCodeURL = "/user/getOtp";
    private static final String loginURL = "/user/login";
    private static final String registerURL = "/user/register";

    public static String getGetOtpCodeURL() {
        return getOtpCodeURL;
    }

    public static String getLoginURL() {
        return loginURL;
    }

    public static String getRegisterURL() {
        return registerURL;
    }
}
