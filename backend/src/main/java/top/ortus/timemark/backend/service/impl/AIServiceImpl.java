package top.ortus.timemark.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
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
import top.ortus.timemark.backend.utils.HotelDemoDataFactory;
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
    private static final Map<String, CityTravelKnowledge> CITY_TRAVEL_KNOWLEDGE = Map.of(
            "杭州", new CityTravelKnowledge(
                    List.of("西湖", "断桥残雪", "苏堤", "雷峰塔", "灵隐寺", "法喜寺", "西溪湿地", "京杭大运河"),
                    List.of("浙江省博物馆", "中国茶叶博物馆", "南宋官窑博物馆", "杭州工艺美术博物馆"),
                    List.of("河坊街", "南宋御街", "湖滨步行街", "小河直街", "武林夜市"),
                    List.of("片儿川", "葱包桧", "定胜糕", "东坡肉", "西湖醋鱼", "龙井虾仁", "知味小笼", "猫耳朵"),
                    List.of("西湖龙井茶", "藕粉", "丝绸围巾", "王星记扇子")
            ),
            "成都", new CityTravelKnowledge(
                    List.of("宽窄巷子", "锦里", "武侯祠", "杜甫草堂", "人民公园", "青羊宫", "熊猫基地"),
                    List.of("成都博物馆", "四川博物院", "金沙遗址博物馆"),
                    List.of("春熙路", "太古里", "建设巷", "玉林路"),
                    List.of("担担面", "钟水饺", "龙抄手", "甜水面", "钵钵鸡", "兔头", "火锅", "串串香"),
                    List.of("火锅底料", "蜀锦", "熊猫文创", "郫县豆瓣")
            ),
            "北京", new CityTravelKnowledge(
                    List.of("故宫", "天安门广场", "景山公园", "颐和园", "天坛", "什刹海", "八达岭长城"),
                    List.of("国家博物馆", "首都博物馆", "中国美术馆"),
                    List.of("南锣鼓巷", "前门大街", "王府井", "五道营胡同"),
                    List.of("北京烤鸭", "炸酱面", "卤煮", "豆汁焦圈", "驴打滚", "豌豆黄", "涮羊肉"),
                    List.of("稻香村点心", "景泰蓝文创", "故宫文创", "北京果脯")
            ),
            "上海", new CityTravelKnowledge(
                    List.of("外滩", "陆家嘴", "豫园", "南京路步行街", "武康路", "田子坊", "朱家角"),
                    List.of("上海博物馆", "上海历史博物馆", "中华艺术宫"),
                    List.of("新天地", "淮海路", "城隍庙", "愚园路"),
                    List.of("生煎包", "小笼包", "葱油拌面", "排骨年糕", "本帮红烧肉", "蟹粉面"),
                    List.of("蝴蝶酥", "梨膏糖", "大白兔奶糖", "上海文创")
            ),
            "西安", new CityTravelKnowledge(
                    List.of("秦始皇兵马俑", "西安城墙", "大雁塔", "钟鼓楼", "华清宫", "大唐不夜城"),
                    List.of("陕西历史博物馆", "西安博物院", "碑林博物馆"),
                    List.of("回民街", "永兴坊", "书院门", "小南门早市"),
                    List.of("肉夹馍", "羊肉泡馍", "凉皮", "biangbiang面", "葫芦鸡", "甑糕", "胡辣汤"),
                    List.of("秦俑文创", "石榴", "陕北剪纸", "黄桂稠酒")
            ),
            "三亚", new CityTravelKnowledge(
                    List.of("亚龙湾", "蜈支洲岛", "天涯海角", "鹿回头", "南山文化旅游区", "椰梦长廊"),
                    List.of("三亚千古情", "海南省民族博物馆"),
                    List.of("第一市场", "解放路步行街", "后海村", "海棠湾"),
                    List.of("清补凉", "椰子鸡", "海南粉", "抱罗粉", "文昌鸡", "和乐蟹", "陵水酸粉"),
                    List.of("椰子制品", "黄灯笼辣椒酱", "热带水果干", "珍珠饰品")
            )
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
        String prompt = renderPrompt(
                hotelRecommendPrompt,
                "用户需求：%s。请提取以下字段：目的地、价格上限、房间数、入住人数、偏好设施、星级要求。返回 JSON 格式。",
                safeInput
        );
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
        List<String> comments;
        try {
            comments = jdbcTemplate.query(
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
        } catch (DataAccessException ex) {
            log.warn("Read review table failed, using demo reviews, hotelId={}, err={}", hotelId, ex.getMessage());
            comments = List.of();
        }
        if (comments.isEmpty()) {
            comments = demoReviewContents(hotelId);
        }

        String prompt = renderPrompt(
                reviewSummaryPrompt,
                "请分析以下酒店评论，提取优点、缺点和总体评价，以 JSON 格式返回：{ \"pros\": [], \"cons\": [], \"overall\": \"\" }。评论内容：%s",
                String.join("\n", comments)
        );
        log.info("AI review summary prompt={}, length={}", mask(prompt), prompt.length());

        final List<String> finalComments = comments;
        try {
            return aiClient.chat(prompt)
                    .map(this::parseReviewSummary)
                    .orElseGet(() -> fallbackReviewSummary(finalComments));
        } catch (Exception ex) {
            log.warn("AI review summary fallback, hotelId={}, err={}", hotelId, ex.getMessage());
            return fallbackReviewSummary(finalComments);
        }
    }

    private List<String> demoReviewContents(Long hotelId) {
        try {
            HotelVO hotel = hotelService.getHotel(hotelId);
            return HotelDemoDataFactory.buildReviewContents(hotelId, hotel.getName(), hotel.getAddress());
        } catch (Exception ex) {
            return HotelDemoDataFactory.buildReviewContents(hotelId, "这家酒店", "核心商圈");
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
        String prompt = renderPrompt(travelPlanPrompt, "请生成旅行行程 JSON：%s", userNeed);
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

    private String renderPrompt(Resource resource, String fallback, String value) {
        String template = loadPrompt(resource, fallback);
        String safeValue = value == null ? "" : value;
        if (template.contains("%s")) {
            return template.replace("%s", safeValue);
        }
        return template + "\n" + safeValue;
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
        CityTravelKnowledge knowledge = travelKnowledge(destination);
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 1; i <= days; i++) {
            String theme = i == 1 ? "抵达与初见" : (i == days ? "收尾与返程" : "深度体验");
            List<String> items = new ArrayList<>();
            List<String> classicPlaces = new ArrayList<>();
            List<String> museums = new ArrayList<>();
            List<String> foods = new ArrayList<>();
            List<String> souvenirs = new ArrayList<>();
            if (i == 1) {
                List<String> places = pick(knowledge.classicPlaces(), 0, 2);
                List<String> streets = pick(knowledge.streets(), 0, 2);
                List<String> dayFoods = pick(knowledge.foods(), 0, 3);
                items.add("上午/中午抵达" + destination + "并办理入住，优先选择靠近" + firstOrFallback(places, "主要景区") + "或地铁站的住宿");
                items.add("下午从" + join(places) + "开始轻量游览，控制步行强度，预留拍照和休息时间");
                items.add("晚上去" + firstOrFallback(streets, "热门街区") + "附近用餐，重点尝试" + join(dayFoods));
                classicPlaces.addAll(places);
                classicPlaces.addAll(streets);
                foods.addAll(dayFoods);
            } else if (i == days) {
                List<String> places = pick(knowledge.classicPlaces(), Math.max(0, i + 1), 1);
                List<String> streets = pick(knowledge.streets(), 1, 1);
                List<String> dayFoods = pick(knowledge.foods(), Math.max(0, knowledge.foods().size() - 3), 3);
                List<String> daySouvenirs = pick(knowledge.souvenirs(), 0, 3);
                items.add("上午补充游览" + firstOrFallback(places, firstOrFallback(streets, "当地街区")) + "，安排轻量路线，避免返程日过满");
                items.add("中午选择" + join(dayFoods) + "等方便控制时间的本地餐食");
                items.add("下午整理行李并预留前往车站/机场时间，返程前购买" + join(daySouvenirs));
                classicPlaces.addAll(places);
                classicPlaces.addAll(streets);
                foods.addAll(dayFoods);
                souvenirs.addAll(daySouvenirs);
            } else {
                List<String> places = pick(knowledge.classicPlaces(), i, 2);
                List<String> dayMuseums = pick(knowledge.museums(), i - 2, 1);
                List<String> streets = pick(knowledge.streets(), i, 1);
                List<String> dayFoods = pick(knowledge.foods(), i * 2, 3);
                items.add("上午游览" + join(places) + "，热门景点建议提前预约门票或错峰进入");
                items.add("下午安排" + firstOrFallback(dayMuseums, "当地博物馆") + "，之后转去" + firstOrFallback(streets, "文化街区") + "散步休息");
                items.add("晚上体验" + join(dayFoods) + "，如体力允许可补充夜景或商圈路线");
                classicPlaces.addAll(places);
                classicPlaces.addAll(streets);
                museums.addAll(dayMuseums);
                foods.addAll(dayFoods);
            }
            String tip = "注意根据实时天气调整；" + (StringUtils.hasText(preferences) ? "已结合偏好：" + preferences + "；" : "")
                    + (StringUtils.hasText(budget) ? "预算参考：" + budget : "费用按现场价格为准");
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("day", i);
            day.put("theme", theme);
            day.put("items", items);
            putIfNotEmpty(day, "classicPlaces", classicPlaces);
            putIfNotEmpty(day, "museums", museums);
            putIfNotEmpty(day, "foods", foods);
            putIfNotEmpty(day, "souvenirs", souvenirs);
            day.put("tips", tip);
            day.put("notes", "");
            data.add(day);
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private void putIfNotEmpty(Map<String, Object> target, String key, List<String> value) {
        if (value != null && !value.isEmpty()) {
            target.put(key, value);
        }
    }

    private CityTravelKnowledge travelKnowledge(String destination) {
        String safeDestination = destination == null ? "" : destination;
        return CITY_TRAVEL_KNOWLEDGE.entrySet().stream()
                .filter(entry -> safeDestination.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(() -> new CityTravelKnowledge(
                        List.of(safeDestination + "热门景区", safeDestination + "历史街区", safeDestination + "城市公园"),
                        List.of(safeDestination + "博物馆"),
                        List.of(safeDestination + "老街", safeDestination + "商业街"),
                        List.of(safeDestination + "特色面点", safeDestination + "本地家常菜", safeDestination + "季节小吃"),
                        List.of(safeDestination + "文创礼品", safeDestination + "地方特产")
                ));
    }

    private List<String> pick(List<String> values, int start, int count) {
        if (values == null || values.isEmpty() || count <= 0) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(values.get(Math.floorMod(start + i, values.size())));
        }
        return result.stream().distinct().toList();
    }

    private String firstOrFallback(List<String> values, String fallback) {
        return values == null || values.isEmpty() ? fallback : values.get(0);
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty() ? "当地特色体验" : String.join("、", values);
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

    private record CityTravelKnowledge(
            List<String> classicPlaces,
            List<String> museums,
            List<String> streets,
            List<String> foods,
            List<String> souvenirs
    ) {
    }
}
