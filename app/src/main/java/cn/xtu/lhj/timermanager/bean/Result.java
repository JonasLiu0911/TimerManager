package cn.xtu.lhj.timermanager.bean;

public class Result {

    private Integer resultId;
    private String userTel;
    private Long beginTime;
    private Long finishTime;
    private String resultDesc;
    private String resultTag;

    public Integer getResultId() {
        return resultId;
    }

    public void setResultId(Integer resultId) {
        this.resultId = resultId;
    }

    public String getUserTel() {
        return userTel;
    }

    public void setUserTel(String userTel) {
        this.userTel = userTel;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Long beginTime) {
        this.beginTime = beginTime;
    }

    public Long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }

    public String getResultTag() {
        return resultTag;
    }

    public void setResultTag(String resultTag) {
        this.resultTag = resultTag;
    }
}
