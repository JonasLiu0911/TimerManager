package cn.xtu.lhj.timermanager.constant;

public class NetConstant {

    public static final String baseUrl = "http://498ua00061.qicp.vip";

    private static final String getOtpCodeURL = "/user/getOtp";
    private static final String loginURL = "/user/login";
    private static final String registerURL = "/user/register";

    private static final String getUserInfoURL = "/user/getUserInfo";
    private static final String updateNicknameURL = "/user/updateUserName";
    private static final String updateGenderURL = "/user/updateUserGender";
    private static final String updateAgeURL = "/user/updateUserAge";

    public static String getGetOtpCodeURL() {
        return getOtpCodeURL;
    }

    public static String getLoginURL() {
        return loginURL;
    }

    public static String getRegisterURL() {
        return registerURL;
    }

    public static String getGetUserInfoURL() {
        return getUserInfoURL;
    }

    public static String getUpdateNicknameURL() {
        return updateNicknameURL;
    }

    public static String getUpdateGenderURL() {
        return updateGenderURL;
    }

    public static String getUpdateAgeURL() {
        return updateAgeURL;
    }
}
