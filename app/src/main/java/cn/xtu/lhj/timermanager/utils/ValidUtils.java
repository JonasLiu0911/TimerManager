package cn.xtu.lhj.timermanager.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidUtils {

    //校验账号不能为空，必须是中国大陆手机号
    public static boolean isPhoneValid(String account) {
        if (account == null) {
            return false;
        }

        String pattern = "^[1]([3-9])[0-9]{9}$";
        Pattern ptr = Pattern.compile(pattern);
        Matcher mtc = ptr.matcher(account);
        return mtc.matches();
    }

    //校验密码不少于6位
    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() >= 6;
    }

    //校验性别
    public static boolean isGenderValid(String gender) {
        return gender.equals("1") || gender.equals("2");
    }

    //MD5加密+Base64编码
    public static String encodeByMD5(String pwd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String newPwd = new String(Base64.encode(md5.digest(pwd.getBytes("UTF-8")), Base64.NO_WRAP));
        return newPwd;
    }
}
