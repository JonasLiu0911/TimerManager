package cn.xtu.lhj.timermanager.bean;

import java.math.BigDecimal;

public class Location {
    private Integer id;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String tel;
    private Long timeX;

    public Location(Integer id,
                    BigDecimal longitude,
                    BigDecimal latitude,
                    String tel,
                    Long timeX) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.tel = tel;
        this.timeX = timeX;
    }

    public Location() {

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

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Long getTimeX() {
        return timeX;
    }

    public void setTimeX(Long timeX) {
        this.timeX = timeX;
    }
}
