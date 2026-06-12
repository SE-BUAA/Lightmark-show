package top.ortus.lightmark.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

public abstract class BaseIntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtTokenService jwtTokenService;

    protected String bearerToken(long userId, String nickname, List<String> roles) {
        return "Bearer " + jwtTokenService.createToken(userId, nickname, roles);
    }
}
