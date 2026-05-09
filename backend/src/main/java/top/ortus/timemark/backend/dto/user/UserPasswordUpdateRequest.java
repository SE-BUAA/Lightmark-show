package top.ortus.timemark.backend.dto.user;

public class UserPasswordUpdateRequest {
    private String oldPassword;
    private String newPassword;

    public UserPasswordUpdateRequest() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
