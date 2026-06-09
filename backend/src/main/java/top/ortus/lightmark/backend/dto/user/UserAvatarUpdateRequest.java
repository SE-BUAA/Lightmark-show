package top.ortus.lightmark.backend.dto.user;

public class UserAvatarUpdateRequest {
    private String avatarUrl;

    public UserAvatarUpdateRequest() {
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
