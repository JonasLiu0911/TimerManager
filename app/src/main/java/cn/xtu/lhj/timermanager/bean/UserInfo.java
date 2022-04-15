package cn.xtu.lhj.timermanager.bean;

public class UserInfo {

    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
    private String telephone;
    private String headUrl;

    public UserInfo() {
    }

    public UserInfo(String telephone, Byte gender, Integer age, String name, String headUrl) {
        this.telephone = telephone;
        this.gender = gender;
        this.age = age;
        this.name = name;
        this.headUrl = headUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }
}
