package top.ortus.timemark.backend.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import top.ortus.timemark.backend.BackendApplication;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dao.UserRepositoryImpl;
import top.ortus.timemark.backend.dto.user.UserPasswordUpdateRequest;
import top.ortus.timemark.backend.security.UserIdentity;

@SpringBootTest(classes = BackendApplication.class)
public class UserPasswordControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
    public void testUpdatePassword() {
        String token = jwtTokenService.createToken(2L, "普通用户", UserIdentity.USER);
        String authorization = "Bearer " + token;

        UserPasswordUpdateRequest req = new UserPasswordUpdateRequest();
        req.setOldPassword("password");
        req.setNewPassword("new-password-123");

        ApiResponse<Boolean> resp = userController.updatePassword(authorization, req);
        Assertions.assertEquals(0, resp.getCode());
        Assertions.assertTrue(Boolean.TRUE.equals(resp.getData()));

        String hashed = userRepository.findById("2").getPassword();
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("new-password-123", hashed));
    }
}

