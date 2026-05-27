package top.ortus.timemark.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.security.UserIdentity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserLevelUpgradeInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    public void levelUpgradeInfoShouldReturnNonZeroPointsNeeded() throws Exception {
        String token = jwtTokenService.createToken(2L, "普通用户", UserIdentity.USER);
        mockMvc.perform(get("/api/user/level/upgrade-info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.level").value(1))
                .andExpect(jsonPath("$.data.pointsNeeded").value(380));
    }
}

