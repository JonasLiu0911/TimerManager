package cn.xtu.lhj.timermanager.bean;

import java.math.BigDecimal;
import java.util.Date;

public class Schedule {
    private Integer id;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer userId;
    private String scheduleTitle;
    private String scheduleInfo;
    private Date scheduleStartTime;
    private Date createTime;
    private Date updateTime;

    public Schedule(Integer id,
                    BigDecimal longitude,
                    BigDecimal latitude,
                    Integer userId,
                    String scheduleTitle,
                    String scheduleInfo,
                    Date scheduleStartTime,
                    Date createTime,
                    Date updateTime) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.userId = userId;
        this.scheduleTitle = scheduleTitle;
        this.scheduleInfo = scheduleInfo;
        this.scheduleStartTime = scheduleStartTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public Date getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(Date scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
