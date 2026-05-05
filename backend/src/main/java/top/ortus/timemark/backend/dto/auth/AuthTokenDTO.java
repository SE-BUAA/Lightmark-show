package top.ortus.timemark.backend.dto.auth;

public class AuthTokenDTO {
    private String token;
    private String userId;
    private String nickname;

    public AuthTokenDTO() {
    }

    public AuthTokenDTO(String token, String userId, String nickname) {
        this.token = token;
        this.userId = userId;
        this.nickname = nickname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}

