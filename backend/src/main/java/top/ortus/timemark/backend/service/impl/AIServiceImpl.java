package top.ortus.timemark.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import top.ortus.timemark.backend.common.PageResult;
import top.ortus.timemark.backend.dto.AIHotelSearchIntent;
import top.ortus.timemark.backend.dto.AIRecommendResultVO;
import top.ortus.timemark.backend.dto.HotelSearchDTO;
import top.ortus.timemark.backend.dto.ReviewSummaryVO;
import top.ortus.timemark.backend.dto.module.TravelPlanDTO;
import top.ortus.timemark.backend.service.AIService;
import top.ortus.timemark.backend.service.HotelService;
import top.ortus.timemark.backend.utils.AIClient;
import top.ortus.timemark.backend.vo.HotelVO;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private static final String AI_BUSY = "AI 服务繁忙，请稍后再试。已为您返回规则推荐结果。";
    private static final List<String> SUPPORTED_CITIES = List.of("北京", "上海", "成都", "杭州", "三亚", "西安");
    private static final List<String> PREFERENCE_WORDS = List.of(
            "安静", "干净", "亲子", "早餐", "停车", "健身", "地铁", "交通", "服务", "海景", "湖景", "迪士尼", "景区", "商务"
    );
    private static final Map<Character, Integer> CHINESE_DIGITS = Map.of(
            '一', 1, '二', 2, '两', 2, '三', 3, '四', 4, '五', 5, '六', 6, '七', 7, '八', 8, '九', 9
    );

    private final HotelService hotelService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final AIClient aiClient;
    private final Resource hotelRecommendPrompt;
    private final Resource reviewSummaryPrompt;
    private final Resource travelPlanPrompt;

    AIServiceImpl(HotelService hotelService,
                  JdbcTemplate jdbcTemplate,
                  ObjectMapper objectMapper,
                  AIClient aiClient,
                  Resource hotelRecommendPrompt,
                  Resource reviewSummaryPrompt) {
        this(hotelService, jdbcTemplate, objectMapper, aiClient, hotelRecommendPrompt, reviewSummaryPrompt,
                new ByteArrayResource("用户需求：%s".getBytes(StandardCharsets.UTF_8)));
    }

    @Autowired
    public AIServiceImpl(HotelService hotelService,
                         JdbcTemplate jdbcTemplate,
                         ObjectMapper objectMapper,
                         AIClient aiClient,
                         @Value("classpath:prompts/hotel_recommend.txt") Resource hotelRecommendPrompt,
                         @Value("classpath:prompts/review_summary.txt") Resource reviewSummaryPrompt,
                         @Value("classpath:prompts/travel_plan_generate.txt") Resource travelPlanPrompt) {
        this.hotelService = hotelService;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.aiClient = aiClient;
        this.hotelRecommendPrompt = hotelRecommendPrompt;
        this.reviewSummaryPrompt = reviewSummaryPrompt;
        this.travelPlanPrompt = travelPlanPrompt;
    }

    @Override
    @Transactional(readOnly = true)
    public AIRecommendResultVO recommendHotel(String userInput) {
        long start = System.currentTimeMillis();
        String safeInput = userInput == null ? "" : userInput.trim();
        String prompt = String.format(loadPrompt(
                hotelRecommendPrompt,
                "用户需求：%s。请提取以下字段：目的地、价格上限、房间数、入住人数、偏好设施、星级要求。返回 JSON 格式。"
        ), safeInput);
        log.info("AI hotel recommend prompt={}, length={}", mask(prompt), prompt.length());

        boolean modelUsed = true;
        AIHotelSearchIntent intent;
        try {
            intent = aiClient.chat(prompt)
                    .map(this::parseHotelIntent)
                    .orElseThrow(() -> new IllegalStateException("ai_unavailable"));
        } catch (Exception ex) {
            modelUsed = false;
            log.warn("AI hotel recommend fallback: {}", ex.getMessage());
            intent = fallbackHotelIntent(safeInput);
        }
        normalizeIntent(intent, safeInput);

        HotelSearchDTO searchDTO = HotelSearchDTO.builder()
                .keyword(intent.getDestination())
                .maxPrice(intent.getMaxPrice())
                .roomNum(intent.getRoomNum())
                .adultNum(intent.getAdultNum())
                .facility(firstFacility(intent.getFacilities()))
                .starLevel(intent.getStarLevel())
                .page(1)
                .size(20)
                .sort("rating_desc")
                .build();

        PageResult<HotelVO> page = hotelService.searchHotels(0L, searchDTO);
        List<HotelVO> hotels = rankHotels(
                page.getRecords() == null ? List.of() : page.getRecords(),
                intent
        );

        log.info("AI hotel recommend cost={}ms, modelUsed={}, resultSize={}",
                System.currentTimeMillis() - start, modelUsed, hotels.size());
        return AIRecommendResultVO.builder()
                .recommendText(buildRecommendText(intent, hotels, modelUsed))
                .hotels(hotels)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryVO generateReviewSummary(Long hotelId) {
        if (hotelId == null) {
            return fallbackReviewSummary(List.of());
        }
        List<String> comments = jdbcTemplate.query(
                """
                select content
                from review
                where product_id = ?
                  and status = 1
                  and (target_type is null or target_type = 'HOTEL' or target_type = '')
                order by create_time desc
                limit 20
                """,
                (rs, rowNum) -> rs.getString("content"),
                hotelId
        );
        if (comments.isEmpty()) {
            return ReviewSummaryVO.builder()
                    .pros(List.of())
                    .cons(List.of())
                    .overall("暂无评论，建议结合价格、位置和房型综合判断。")
                    .build();
        }

        String prompt = String.format(loadPrompt(
                reviewSummaryPrompt,
                "请分析以下酒店评论，提取优点、缺点和总体评价，以 JSON 格式返回：{ \"pros\": [], \"cons\": [], \"overall\": \"\" }。评论内容：%s"
        ), String.join("\n", comments));
        log.info("AI review summary prompt={}, length={}", mask(prompt), prompt.length());

        try {
            return aiClient.chat(prompt)
                    .map(this::parseReviewSummary)
                    .orElseGet(() -> fallbackReviewSummary(comments));
        } catch (Exception ex) {
            log.warn("AI review summary fallback, hotelId={}, err={}", hotelId, ex.getMessage());
            return fallbackReviewSummary(comments);
        }
    }

    @Override
    public TravelPlanDTO generateTravelPlan(Map<String, Object> payload) {
        Map<String, Object> safePayload = payload == null ? Map.of() : payload;
        String destination = textFrom(safePayload, "destination", "目的地");
        int days = Math.max(1, Math.min(numberFrom(safePayload, 3, "days", "天数"), 14));
        LocalDate startDate = dateFrom(safePayload, "startDate", "start_date", "date");
        String preferences = textFrom(safePayload, "preferences", "偏好");
        String budget = textFrom(safePayload, "budget", "预算");
        String userNeed = "目的地=" + valueOrDefault(destination, "未填写")
                + "；天数=" + days
                + "；出发日期=" + (startDate == null ? "未填写" : startDate)
                + "；预算=" + valueOrDefault(budget, "未填写")
                + "；偏好=" + valueOrDefault(preferences, "未填写");
        String prompt = String.format(loadPrompt(travelPlanPrompt, "请生成旅行行程 JSON：%s"), userNeed);
        log.info("AI travel plan prompt={}, length={}", mask(prompt), prompt.length());
        try {
            return aiClient.chat(prompt)
                    .map(response -> parseTravelPlan(response, destination, days, startDate))
                    .orElseGet(() -> fallbackTravelPlan(destination, days, startDate, preferences, budget));
        } catch (Exception ex) {
            log.warn("AI travel plan fallback: {}", ex.getMessage());
            return fallbackTravelPlan(destination, days, startDate, preferences, budget);
        }
    }

    AIHotelSearchIntent parseHotelIntent(String response) {
        try {
            JsonNode node = parseModelJson(response);
            return AIHotelSearchIntent.builder()
                    .destination(textValue(node, "destination", "目的地"))
                    .maxPrice(decimalValue(node, "maxPrice", "价格上限", "max_price"))
                    .roomNum(intValue(node, "roomNum", "房间数", "room_num"))
                    .adultNum(intValue(node, "adultNum", "入住人数", "adult_num"))
                    .facilities(stringList(firstNode(node, "facilities", "偏好设施")))
                    .starLevel(intValue(node, "starLevel", "星级要求", "star_level"))
                    .recommendText(textValue(node, "recommendText", "推荐语"))
                    .build();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid_ai_hotel_json", ex);
        }
    }

    ReviewSummaryVO parseReviewSummary(String response) {
        try {
            JsonNode node = parseModelJson(response);
            return ReviewSummaryVO.builder()
                    .pros(stringList(firstNode(node, "pros", "优点")))
                    .cons(stringList(firstNode(node, "cons", "缺点")))
                    .overall(textValue(node, "overall", "总体评价"))
                    .build();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid_ai_review_json", ex);
        }
    }

    TravelPlanDTO parseTravelPlan(String response, String fallbackDestination, int fallbackDays, LocalDate fallbackStartDate) {
        try {
            JsonNode node = parseModelJson(response);
            String destination = textValue(node, "destination", "目的地");
            LocalDate startDate = parseDate(textValue(node, "start_date", "startDate"));
            LocalDate endDate = parseDate(textValue(node, "end_date", "endDate"));
            JsonNode planData = firstNode(node, "plan_data", "planData");
            TravelPlanDTO dto = new TravelPlanDTO();
            dto.setTitle(valueOrDefault(textValue(node, "title", "标题"), valueOrDefault(destination, fallbackDestination) + "智能行程"));
            dto.setDestination(valueOrDefault(destination, fallbackDestination));
            dto.setStart_date(startDate == null ? fallbackStartDate : startDate);
            dto.setEnd_date(endDate == null ? endDateFrom(dto.getStart_date(), fallbackDays) : endDate);
            dto.setPlan_data(planData == null ? fallbackPlanData(valueOrDefault(destination, fallbackDestination), fallbackDays) : objectMapper.writeValueAsString(planData));
            dto.setIs_public(0);
            return dto;
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid_ai_travel_plan_json", ex);
        }
    }

    private JsonNode parseModelJson(String response) throws Exception {
        JsonNode root = objectMapper.readTree(extractJsonText(response));
        JsonNode content = root.at("/choices/0/message/content");
        if (!content.isMissingNode() && content.isTextual()) {
            return objectMapper.readTree(extractJsonText(content.asText()));
        }
        if (root.hasNonNull("content") && root.get("content").isTextual()) {
            return objectMapper.readTree(extractJsonText(root.get("content").asText()));
        }
        return root;
    }

    private String extractJsonText(String value) {
        if (value == null) {
            return "{}";
        }
        String text = value.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        return start >= 0 && end > start ? text.substring(start, end + 1) : text;
    }

    private void normalizeIntent(AIHotelSearchIntent intent, String userInput) {
        if (!StringUtils.hasText(intent.getDestination())) {
            intent.setDestination(extractDestination(userInput));
        }
        if (intent.getMaxPrice() == null) {
            intent.setMaxPrice(extractBudget(userInput));
        }
        if (intent.getAdultNum() == null) {
            intent.setAdultNum(extractPeopleCount(userInput));
        }
        if (intent.getRoomNum() == null && intent.getAdultNum() != null) {
            intent.setRoomNum(Math.max(1, (int) Math.ceil(intent.getAdultNum() / 2.0)));
        }
        if (intent.getStarLevel() == null) {
            intent.setStarLevel(extractStarLevel(userInput));
        }
        List<String> merged = new ArrayList<>(intent.getFacilities() == null ? List.of() : intent.getFacilities());
        for (String preference : PREFERENCE_WORDS) {
            if (userInput != null && userInput.contains(preference) && !merged.contains(preference)) {
                merged.add(preference);
            }
        }
        intent.setFacilities(merged);
    }

    private AIHotelSearchIntent fallbackHotelIntent(String userInput) {
        return AIHotelSearchIntent.builder()
                .destination(extractDestination(userInput))
                .maxPrice(extractBudget(userInput))
                .adultNum(extractPeopleCount(userInput))
                .roomNum(extractRoomCount(userInput))
                .starLevel(extractStarLevel(userInput))
                .facilities(PREFERENCE_WORDS.stream()
                        .filter(word -> userInput != null && userInput.contains(word))
                        .toList())
                .recommendText(AI_BUSY)
                .build();
    }

    private List<HotelVO> rankHotels(List<HotelVO> hotels, AIHotelSearchIntent intent) {
        return hotels.stream()
                .filter(hotel -> intent.getMaxPrice() == null
                        || hotel.getPriceMin() == null
                        || hotel.getPriceMin().compareTo(intent.getMaxPrice()) <= 0)
                .sorted(Comparator.comparingInt((HotelVO hotel) -> preferenceScore(hotel, intent.getFacilities())).reversed()
                        .thenComparing(hotel -> hotel.getPriceMin() == null ? BigDecimal.valueOf(999999) : hotel.getPriceMin())
                        .thenComparing(hotel -> hotel.getRating() == null ? 0D : -hotel.getRating()))
                .limit(5)
                .toList();
    }

    private int preferenceScore(HotelVO hotel, List<String> preferences) {
        if (hotel == null || preferences == null || preferences.isEmpty()) {
            return 0;
        }
        String hotelText = String.join(" ",
                value(hotel.getName()),
                value(hotel.getAddress()),
                hotel.getFacilities() == null ? "" : String.join(" ", hotel.getFacilities()),
                value(hotel.getCancelPolicy())
        );
        int score = 0;
        for (String preference : preferences) {
            if (hotelText.contains(preference)) {
                score += 3;
            }
            score += reviewPreferenceHits(hotel.getId(), preference) * 2;
        }
        return score;
    }

    private int reviewPreferenceHits(String hotelId, String preference) {
        if (!StringUtils.hasText(hotelId) || !StringUtils.hasText(preference)) {
            return 0;
        }
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    select count(1)
                    from review
                    where product_id = ?
                      and status = 1
                      and content like concat('%', ?, '%')
                    """,
                    Integer.class,
                    Long.valueOf(hotelId),
                    preference
            );
            return count == null ? 0 : Math.min(count, 5);
        } catch (Exception ex) {
            return 0;
        }
    }

    private ReviewSummaryVO fallbackReviewSummary(List<String> comments) {
        String joined = String.join(" ", comments);
        List<String> pros = new ArrayList<>();
        List<String> cons = new ArrayList<>();
        if (containsAny(joined, "位置", "交通", "地铁", "方便")) {
            pros.add("位置好");
        }
        if (containsAny(joined, "干净", "整洁", "卫生")) {
            pros.add("干净");
        }
        if (containsAny(joined, "服务", "热情", "贴心")) {
            pros.add("服务好");
        }
        if (containsAny(joined, "早餐", "餐厅")) {
            pros.add("早餐体验较好");
        }
        if (containsAny(joined, "吵", "噪音", "隔音")) {
            cons.add("噪音或隔音问题");
        }
        if (containsAny(joined, "旧", "老", "设施")) {
            cons.add("设施可能偏旧");
        }
        if (pros.isEmpty()) {
            pros.add("整体体验稳定");
        }
        long positive = comments.stream().filter(item -> containsAny(item, "好", "满意", "推荐", "干净", "方便")).count();
        int recommendRate = comments.isEmpty() ? 0 : (int) Math.round(positive * 100.0 / comments.size());
        return ReviewSummaryVO.builder()
                .pros(pros)
                .cons(cons)
                .overall("AI 服务繁忙，请稍后再试。基于规则统计的总体推荐率约" + recommendRate + "%。")
                .build();
    }

    private String buildRecommendText(AIHotelSearchIntent intent, List<HotelVO> hotels, boolean modelUsed) {
        if (hotels.isEmpty()) {
            return modelUsed ? "暂未找到完全匹配的酒店，可以尝试放宽预算、星级或设施条件。" : AI_BUSY;
        }
        if (modelUsed && StringUtils.hasText(intent.getRecommendText())) {
            return intent.getRecommendText();
        }
        List<String> conditions = new ArrayList<>();
        if (StringUtils.hasText(intent.getDestination())) {
            conditions.add(intent.getDestination());
        }
        if (intent.getMaxPrice() != null) {
            conditions.add("预算不超过" + intent.getMaxPrice().stripTrailingZeros().toPlainString() + "元");
        }
        if (intent.getAdultNum() != null) {
            conditions.add(intent.getAdultNum() + "人入住");
        }
        if (intent.getFacilities() != null && !intent.getFacilities().isEmpty()) {
            conditions.add("偏好" + String.join("、", intent.getFacilities()));
        }
        String prefix = modelUsed ? "根据您的要求" : AI_BUSY + " 根据您的要求";
        return prefix + (conditions.isEmpty() ? "" : "（" + String.join("、", conditions) + "）")
                + "，推荐以下酒店：" + hotels.stream()
                .map(HotelVO::getName)
                .filter(StringUtils::hasText)
                .limit(3)
                .reduce((left, right) -> left + "、" + right)
                .orElse("热门酒店");
    }

    private String loadPrompt(Resource resource, String fallback) {
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private TravelPlanDTO fallbackTravelPlan(String destination, int days, LocalDate startDate, String preferences, String budget) {
        String safeDestination = StringUtils.hasText(destination) ? destination.trim() : "目的地";
        TravelPlanDTO dto = new TravelPlanDTO();
        dto.setTitle(safeDestination + days + "日智能行程");
        dto.setDestination(safeDestination);
        dto.setStart_date(startDate);
        dto.setEnd_date(endDateFrom(startDate, days));
        dto.setPlan_data(fallbackPlanData(safeDestination, days, preferences, budget));
        dto.setIs_public(0);
        return dto;
    }

    private String fallbackPlanData(String destination, int days) {
        return fallbackPlanData(destination, days, "", "");
    }

    private String fallbackPlanData(String destination, int days, String preferences, String budget) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 1; i <= days; i++) {
            String theme = i == 1 ? "抵达与初见" : (i == days ? "收尾与返程" : "深度体验");
            List<String> items = new ArrayList<>();
            if (i == 1) {
                items.add("抵达" + destination + "并办理入住");
                items.add("安排轻松市区散步，熟悉交通和周边");
                items.add("晚餐选择当地代表性美食");
            } else if (i == days) {
                items.add("上午补充一个轻量景点或当地市场");
                items.add("整理行李并预留前往车站/机场时间");
                items.add("返程前购买伴手礼");
            } else {
                items.add("上午游览" + destination + "经典景点");
                items.add("下午安排文化街区、博物馆或自然景观");
                items.add("晚上体验夜市、商圈或特色餐厅");
            }
            String tip = "注意根据实时天气调整；" + (StringUtils.hasText(preferences) ? "已结合偏好：" + preferences + "；" : "")
                    + (StringUtils.hasText(budget) ? "预算参考：" + budget : "费用按现场价格为准");
            data.add(Map.of("day", i, "theme", theme, "items", items, "tips", tip));
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private String textFrom(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value).trim();
            }
        }
        return null;
    }

    private int numberFrom(Map<String, Object> payload, int fallback, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value != null) {
                String digits = String.valueOf(value).replaceAll("[^0-9]", "");
                if (StringUtils.hasText(digits)) {
                    return Integer.parseInt(digits);
                }
            }
        }
        return fallback;
    }

    private LocalDate dateFrom(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            LocalDate parsed = parseDate(Objects.toString(payload.get(key), null));
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDate endDateFrom(LocalDate startDate, int days) {
        return startDate == null ? null : startDate.plusDays(Math.max(days, 1) - 1L);
    }

    private String valueOrDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String extractDestination(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        return SUPPORTED_CITIES.stream().filter(input::contains).findFirst().orElse(null);
    }

    private BigDecimal extractBudget(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        List<Pattern> patterns = List.of(
                Pattern.compile("(?:预算|价格|房价|每晚|一晚|不超过|低于|以内|以下)[^0-9]{0,8}(\\d{2,5})"),
                Pattern.compile("(\\d{2,5})\\s*(?:元|块)?\\s*(?:以内|以下|左右)")
        );
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return new BigDecimal(matcher.group(1));
            }
        }
        return null;
    }

    private Integer extractPeopleCount(String input) {
        Integer number = extractNumberBefore(input, "人");
        if (number != null) {
            return number;
        }
        return input != null && (input.contains("情侣") || input.contains("夫妻") || input.contains("双人")) ? 2 : null;
    }

    private Integer extractRoomCount(String input) {
        return extractNumberBefore(input, "间");
    }

    private Integer extractStarLevel(String input) {
        Integer number = extractNumberBefore(input, "星");
        return number != null && number >= 1 && number <= 5 ? number : null;
    }

    private Integer extractNumberBefore(String input, String unit) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        Matcher digit = Pattern.compile("(\\d+)\\s*" + Pattern.quote(unit)).matcher(input);
        if (digit.find()) {
            return Integer.parseInt(digit.group(1));
        }
        Matcher chinese = Pattern.compile("([一二两三四五六七八九])\\s*" + Pattern.quote(unit)).matcher(input);
        return chinese.find() ? CHINESE_DIGITS.get(chinese.group(1).charAt(0)) : null;
    }

    private JsonNode firstNode(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value;
            }
        }
        return null;
    }

    private String textValue(JsonNode node, String... names) {
        JsonNode value = firstNode(node, names);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Integer intValue(JsonNode node, String... names) {
        JsonNode value = firstNode(node, names);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asInt();
        }
        String digits = value.asText().replaceAll("[^0-9]", "");
        return StringUtils.hasText(digits) ? Integer.parseInt(digits) : null;
    }

    private BigDecimal decimalValue(JsonNode node, String... names) {
        JsonNode value = firstNode(node, names);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.decimalValue();
        }
        String digits = value.asText().replaceAll("[^0-9.]", "");
        return StringUtils.hasText(digits) ? new BigDecimal(digits) : null;
    }

    private List<String> stringList(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> {
                if (StringUtils.hasText(item.asText())) {
                    result.add(item.asText());
                }
            });
        } else if (StringUtils.hasText(node.asText())) {
            result.add(node.asText());
        }
        return result;
    }

    private String firstFacility(List<String> facilities) {
        return facilities == null || facilities.isEmpty() ? null : facilities.get(0);
    }

    private boolean containsAny(String value, String... words) {
        if (value == null) {
            return false;
        }
        for (String word : words) {
            if (value.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String mask(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ");
        return normalized.length() <= 180 ? normalized : normalized.substring(0, 180) + "...";
    }
}
