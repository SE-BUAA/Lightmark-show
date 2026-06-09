package top.ortus.lightmark.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import top.ortus.lightmark.backend.dto.AIHotelSearchIntent;
import top.ortus.lightmark.backend.dto.ReviewSummaryVO;
import top.ortus.lightmark.backend.service.HotelService;
import top.ortus.lightmark.backend.utils.AIClient;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AIServiceImplTest {

    private final AIServiceImpl aiService = new AIServiceImpl(
            mock(HotelService.class),
            mock(JdbcTemplate.class),
            new ObjectMapper(),
            mock(AIClient.class),
            resource("用户需求：%s。返回 JSON。"),
            resource("评论内容：%s。返回 JSON。")
    );

    @Test
    void parseHotelIntentFromModelJson() {
        String response = """
                {
                  "destination": "上海迪士尼",
                  "maxPrice": 500,
                  "roomNum": 1,
                  "adultNum": 2,
                  "facilities": ["安静", "亲子"],
                  "starLevel": 4,
                  "recommendText": "为您推荐安静亲子的酒店"
                }
                """;

        AIHotelSearchIntent intent = aiService.parseHotelIntent(response);

        assertThat(intent.getDestination()).isEqualTo("上海迪士尼");
        assertThat(intent.getMaxPrice()).isEqualByComparingTo("500");
        assertThat(intent.getAdultNum()).isEqualTo(2);
        assertThat(intent.getFacilities()).containsExactly("安静", "亲子");
    }

    @Test
    void parseReviewSummaryFromModelJson() {
        String response = """
                {
                  "pros": ["位置好", "干净"],
                  "cons": ["噪音大"],
                  "overall": "总体推荐率80%"
                }
                """;

        ReviewSummaryVO summary = aiService.parseReviewSummary(response);

        assertThat(summary.getPros()).containsExactly("位置好", "干净");
        assertThat(summary.getCons()).containsExactly("噪音大");
        assertThat(summary.getOverall()).isEqualTo("总体推荐率80%");
    }

    private static ByteArrayResource resource(String text) {
        return new ByteArrayResource(text.getBytes(StandardCharsets.UTF_8));
    }
}
