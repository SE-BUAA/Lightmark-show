package top.ortus.timemark.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import top.ortus.timemark.backend.BackendApplication;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = "spring.datasource.url=jdbc:h2:mem:timemark_vacation;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
)
@ActiveProfiles("test")
public class VacationControllerTest {
    @Autowired
    private VacationController vacationController;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSearch() throws Exception {
        System.out.println(objectMapper.writeValueAsString(vacationController.search()));
    }
}
