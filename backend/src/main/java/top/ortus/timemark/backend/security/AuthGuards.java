package top.ortus.timemark.backend.security;

import top.ortus.timemark.backend.exception.BusinessException;

public final class AuthGuards {
    private AuthGuards() {
    }

    public static AuthUser currentUser() {
        AuthUser authUser = AuthContext.get();
        if (authUser == null) {
            throw new BusinessException(401, "未登录或登录状态已过期");
        }
        return authUser;
    }

    public static void requireAdmin() {
        AuthUser authUser = currentUser();
        if (!authUser.isAdmin()) {
            throw new BusinessException(403, "无权限访问管理员接口");
        }
    }
}

