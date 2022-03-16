package cn.xtu.lhj.timermanager.bean;

public class OtpCode {
    private String telephone;
    private String otpCode;

    public OtpCode() {
    }

    public OtpCode(String telephone, String otpCode) {
        this.telephone = telephone;
        this.otpCode = otpCode;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
