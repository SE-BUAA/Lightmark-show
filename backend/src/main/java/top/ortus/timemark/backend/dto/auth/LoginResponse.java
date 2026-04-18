package top.ortus.timemark.backend.dto.auth;

import java.util.List;

public record LoginResponse(
        Long userId,
        String nickname,
        String token,
        List<String> roles
) {
}

