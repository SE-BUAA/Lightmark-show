package top.ortus.timemark.backend.dto.auth;

public class AuthLoginRequest {
    private String account;
    private String password;

    public AuthLoginRequest() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}