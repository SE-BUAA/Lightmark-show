package top.ortus.timemark.backend.security;

import java.util.List;

public record AuthUser(Long userId, String nickname, List<String> roles) {
    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }
}

