package top.ortus.timemark.backend.dto.user;

import java.time.LocalDateTime;

public class UserUpdateRequest {
    private String phone;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private Integer points;
    private Short level;
    private Integer status;
    private String register_source;
    private LocalDateTime last_login_time;
    private String last_login_ip;

    public UserUpdateRequest() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Short getLevel() {
        return level;
    }

    public void setLevel(Short level) {
        this.level = level;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRegister_source() {
        return register_source;
    }

    public void setRegister_source(String register_source) {
        this.register_source = register_source;
    }

    public LocalDateTime getLast_login_time() {
        return last_login_time;
    }

    public void setLast_login_time(LocalDateTime last_login_time) {
        this.last_login_time = last_login_time;
    }

    public String getLast_login_ip() {
        return last_login_ip;
    }

    public void setLast_login_ip(String last_login_ip) {
        this.last_login_ip = last_login_ip;
    }
}