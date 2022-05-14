package cn.xtu.lhj.timermanager.constant;

public class NetConstant {

    public static final String baseUrl = "http://498ua00061.qicp.vip";

    private static final String getOtpCodeURL = "/user/getOtp";
    private static final String loginURL = "/user/login";
    private static final String registerURL = "/user/register";

    private static final String getUserInfoURL = "/user/getUserInfo";
    private static final String updateHeadURL = "/user/updateUserAvatar";
    private static final String updateNicknameURL = "/user/updateUserName";
    private static final String updateGenderURL = "/user/updateUserGender";
    private static final String updateAgeURL = "/user/updateUserAge";
    private static final String updatePwdURL = "/user/updateUserPwd";

    private static final String getScheduleURL = "/schedule/getSchedule";
    private static final String addScheduleURL = "/schedule/addSchedule";
    private static final String updateScheduleURL = "/schedule/updateSchedule";
    private static final String deleteScheduleURL = "/schedule/deleteSchedule";

    private static final String getHistoryURL = "/schedule/getHistory";

    private static final String postLocationURL = "/location/getLocations";
    private static final String getResultsByTelURL = "/location/getResultsByTel";

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

    public static String getUpdatePwdURL() {
        return updatePwdURL;
    }

    public static String getGetScheduleURL() {
        return getScheduleURL;
    }

    public static String getAddScheduleURL() {
        return addScheduleURL;
    }

    public static String getDeleteScheduleURL() {
        return deleteScheduleURL;
    }

    public static String getUpdateScheduleURL() {
        return updateScheduleURL;
    }

    public static String getGetHistoryURL() {
        return getHistoryURL;
    }

    public static String getUpdateHeadURL() {
        return updateHeadURL;
    }

    public static String getPostLocationURL() {
        return postLocationURL;
    }

    public static String getGetResultsByTelURL() {
        return getResultsByTelURL;
    }
}
