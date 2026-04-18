package top.ortus.timemark.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "账号不能为空") String account,
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 32) String password,
        @NotBlank(message = "昵称不能为空") @Size(max = 50) String nickname
) {
}

