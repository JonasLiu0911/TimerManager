package cn.xtu.lhj.timermanager.bean;

public class UserInfo {

    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
    private String telephone;

    public UserInfo() {
    }

    public UserInfo(String telephone, Byte gender, Integer age, String name) {
        this.telephone = telephone;
        this.gender = gender;
        this.age = age;
        this.name = name;
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
}
