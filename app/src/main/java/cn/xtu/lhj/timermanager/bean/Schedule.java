package cn.xtu.lhj.timermanager.bean;

import java.math.BigDecimal;
import java.util.Date;

public class Schedule {
    private Integer id;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String telephone;
    private String scheduleTitle;
    private String scheduleInfo;
    private Long scheduleStartTime;

    public Schedule(Integer id,
                    BigDecimal longitude,
                    BigDecimal latitude,
                    String telephone,
                    String scheduleTitle,
                    String scheduleInfo,
                    Long scheduleStartTime) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.telephone = telephone;
        this.scheduleTitle = scheduleTitle;
        this.scheduleInfo = scheduleInfo;
        this.scheduleStartTime = scheduleStartTime;
    }

    public Schedule() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getScheduleTitle() {
        return scheduleTitle;
    }

    public void setScheduleTitle(String scheduleTitle) {
        this.scheduleTitle = scheduleTitle;
    }

    public String getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(String scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    public Long getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(Long scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }
}
