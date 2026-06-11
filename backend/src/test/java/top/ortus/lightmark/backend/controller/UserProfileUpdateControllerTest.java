package top.ortus.lightmark.backend.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import top.ortus.lightmark.backend.BackendApplication;
import top.ortus.lightmark.backend.JwtTokenService;
import top.ortus.lightmark.backend.common.ApiResponse;
import top.ortus.lightmark.backend.dao.User;
import top.ortus.lightmark.backend.dao.UserRepositoryImpl;
import top.ortus.lightmark.backend.dto.UserDTO;
import top.ortus.lightmark.backend.dto.user.UserUpdateRequest;
import top.ortus.lightmark.backend.security.UserIdentity;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = "spring.datasource.url=jdbc:h2:mem:lightmark_profile_update;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
)
@ActiveProfiles("test")
public class UserProfileUpdateControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
    public void testUpdateCurrentCanClearPhoneAndEmail() {
        String token = jwtTokenService.createToken(2L, "普通用户", UserIdentity.USER);
        String authorization = "Bearer " + token;

        UserUpdateRequest req = new UserUpdateRequest();
        req.setNickname("清空联系方式后");
        req.setPhone("");
        req.setEmail("");

        ApiResponse<UserDTO> resp = userController.updateCurrent(authorization, req);
        Assertions.assertEquals(0, resp.getCode());
        Assertions.assertNotNull(resp.getData());
        Assertions.assertNull(resp.getData().getPhone());
        Assertions.assertNull(resp.getData().getEmail());
        Assertions.assertEquals("清空联系方式后", resp.getData().getNickname());

        User updated = userRepository.findById("2");
        Assertions.assertNull(updated.getPhone());
        Assertions.assertNull(updated.getEmail());
        Assertions.assertEquals("清空联系方式后", updated.getNickname());
    }
}
