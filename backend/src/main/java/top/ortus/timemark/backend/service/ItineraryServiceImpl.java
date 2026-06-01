package top.ortus.timemark.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.module.TravelPlanDTO;
import top.ortus.timemark.backend.exception.ApiException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ItineraryServiceImpl implements ItineraryService {

    private final JdbcTemplate jdbcTemplate;
    private final AIService aiService;

    public ItineraryServiceImpl(JdbcTemplate jdbcTemplate, AIService aiService) {
        this.jdbcTemplate = jdbcTemplate;
        this.aiService = aiService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TravelPlanDTO> listMyPlans(Long userId, Map<String, String> params) {
        int page = positiveInt(params.get("page"), 1);
        int size = Math.min(positiveInt(params.get("size"), 20), 50);
        String keyword = trim(params.get("keyword"));
        String baseWhere = " where user_id = ?";
        Object[] countArgs;
        Object[] listArgs;
        if (StringUtils.hasText(keyword)) {
            baseWhere += " and (title like ? or destination like ?)";
            String like = "%" + keyword + "%";
            countArgs = new Object[]{userId, like, like};
            listArgs = new Object[]{userId, like, like, size, (page - 1) * size};
        } else {
            countArgs = new Object[]{userId};
            listArgs = new Object[]{userId, size, (page - 1) * size};
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from travel_plan" + baseWhere, Long.class, countArgs);
        List<TravelPlanDTO> list = jdbcTemplate.query(
                "select * from travel_plan" + baseWhere + " order by create_time desc limit ? offset ?",
                this::mapTravelPlan,
                listArgs
        );
        return new PageResponse<>(total == null ? 0 : total, page, size, list);
    }

    @Override
    @Transactional
    public TravelPlanDTO createPlan(Long userId, TravelPlanDTO payload) {
        TravelPlanDTO plan = payload == null ? new TravelPlanDTO() : payload;
        validatePlan(plan);
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into travel_plan (user_id, title, destination, start_date, end_date, plan_data, is_public)
                    values (?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, defaultText(plan.getTitle(), plan.getDestination() + "行程"));
            ps.setString(3, plan.getDestination());
            ps.setDate(4, toSqlDate(plan.getStart_date()));
            ps.setDate(5, toSqlDate(plan.getEnd_date()));
            ps.setString(6, defaultPlanData(plan));
            ps.setInt(7, plan.getIs_public());
            return ps;
        }, keyHolder);
        return getOwnedPlan(userId, generatedId(keyHolder));
    }

    @Override
    @Transactional
    public TravelPlanDTO updatePlan(Long userId, Long id, TravelPlanDTO payload) {
        getOwnedPlan(userId, id);
        TravelPlanDTO plan = payload == null ? new TravelPlanDTO() : payload;
        validatePlan(plan);
        jdbcTemplate.update("""
                update travel_plan
                set title = ?, destination = ?, start_date = ?, end_date = ?, plan_data = ?, is_public = ?
                where id = ? and user_id = ?
                """,
                defaultText(plan.getTitle(), plan.getDestination() + "行程"),
                plan.getDestination(),
                toSqlDate(plan.getStart_date()),
                toSqlDate(plan.getEnd_date()),
                defaultPlanData(plan),
                plan.getIs_public(),
                id,
                userId
        );
        return getOwnedPlan(userId, id);
    }

    @Override
    @Transactional
    public boolean deletePlan(Long userId, Long id) {
        getOwnedPlan(userId, id);
        return jdbcTemplate.update("delete from travel_plan where id = ? and user_id = ?", id, userId) > 0;
    }

    @Override
    @Transactional
    public TravelPlanDTO generatePlan(Long userId, Map<String, Object> payload) {
        TravelPlanDTO generated = aiService.generateTravelPlan(payload);
        generated.setUser_id(String.valueOf(userId));
        return generated;
    }

    @Override
    @Transactional
    public Map<String, String> sharePlan(Long userId, Long id) {
        TravelPlanDTO plan = getOwnedPlan(userId, id);
        if (plan.getIs_public() == 0) {
            jdbcTemplate.update("update travel_plan set is_public = 1 where id = ? and user_id = ?", id, userId);
        }
        return Map.of("shortLink", "/itinerary?sharedPlanId=" + id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> exportPlan(Long userId, Long id) {
        getOwnedPlan(userId, id);
        return Map.of("fileUrl", "/api/itinerary/plans/" + id + "/export");
    }

    private TravelPlanDTO getOwnedPlan(Long userId, Long id) {
        List<TravelPlanDTO> plans = jdbcTemplate.query(
                "select * from travel_plan where id = ? and user_id = ?",
                this::mapTravelPlan,
                id,
                userId
        );
        if (plans.isEmpty()) {
            throw new ApiException(404, "行程不存在或无权访问");
        }
        return plans.get(0);
    }

    private TravelPlanDTO mapTravelPlan(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        TravelPlanDTO dto = new TravelPlanDTO();
        dto.setId(String.valueOf(rs.getLong("id")));
        dto.setUser_id(String.valueOf(rs.getLong("user_id")));
        dto.setTitle(rs.getString("title"));
        dto.setDestination(rs.getString("destination"));
        Date startDate = rs.getDate("start_date");
        Date endDate = rs.getDate("end_date");
        dto.setStart_date(startDate == null ? null : startDate.toLocalDate());
        dto.setEnd_date(endDate == null ? null : endDate.toLocalDate());
        dto.setPlan_data(rs.getString("plan_data"));
        dto.setIs_public(rs.getInt("is_public"));
        var createTime = rs.getTimestamp("create_time");
        dto.setCreate_time(createTime == null ? LocalDateTime.now() : createTime.toLocalDateTime());
        return dto;
    }

    private void validatePlan(TravelPlanDTO plan) {
        if (!StringUtils.hasText(plan.getDestination())) {
            throw new ApiException(400, "请填写目的地");
        }
        if (plan.getStart_date() != null && plan.getEnd_date() != null && plan.getEnd_date().isBefore(plan.getStart_date())) {
            throw new ApiException(400, "结束日期不能早于开始日期");
        }
    }

    private String defaultPlanData(TravelPlanDTO plan) {
        if (StringUtils.hasText(plan.getPlan_data())) {
            return plan.getPlan_data();
        }
        return "[{\"day\":1,\"theme\":\"抵达" + plan.getDestination() + "\",\"items\":[\"办理入住\",\"熟悉周边\",\"品尝当地晚餐\"]}]";
    }

    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private int positiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(Objects.toString(value, ""));
            return parsed > 0 ? parsed : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private long generatedId(org.springframework.jdbc.support.GeneratedKeyHolder keyHolder) {
        Number key = keyHolder.getKeyList().stream()
                .map(keys -> keys.get("id"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .findFirst()
                .orElse(null);
        if (key == null && keyHolder.getKeyList().size() == 1) {
            key = keyHolder.getKeyList().get(0).values().stream()
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .findFirst()
                    .orElse(null);
        }
        if (key == null) {
            throw new ApiException(500, "failed to read generated id");
        }
        return key.longValue();
    }
}
