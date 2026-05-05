package top.ortus.timemark.backend.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private String id;
    private String phone;
    private String email;
    private String nickname;

    private String avatar;
    private int points;
    private short level;
    private int status;
    private String register_source;
    private LocalDateTime last_login_time;
    private String last_login_ip;
    private LocalDateTime create_time;
    private LocalDateTime update_time;
    private boolean deleted;


}
