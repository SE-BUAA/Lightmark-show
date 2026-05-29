package top.ortus.timemark.backend.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.dto.module.ProductDTO;
import top.ortus.timemark.backend.service.FlightSearchService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {
    private static final Map<String, String> CITY_CODES = Map.ofEntries(
            Map.entry("北京", "BJS"),
            Map.entry("北京首都", "PEK"),
            Map.entry("北京大兴", "PKX"),
            Map.entry("上海", "SHA"),
            Map.entry("上海虹桥", "SHA"),
            Map.entry("上海浦东", "PVG"),
            Map.entry("广州", "CAN"),
            Map.entry("深圳", "SZX"),
            Map.entry("成都", "CTU"),
            Map.entry("杭州", "HGH"),
            Map.entry("西安", "XIY"),
            Map.entry("香港", "HKG"),
            Map.entry("中国香港", "HKG")
    );

    private final FlightSearchService flightSearchService;

    public AiController(FlightSearchService flightSearchService) {
        this.flightSearchService = flightSearchService;
    }

    @PostMapping("/post/generate")
    public ApiResponse<AiDTO> generatePost() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/review/sentiment")
    public ApiResponse<AiDTO> reviewSentiment() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/review/reply")
    public ApiResponse<AiDTO>  reviewReply() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/qa/chat")
    public ApiResponse<AiDTO> chatQA() {

        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/customer-service/chat")
    public ApiResponse<AiDTO> chatCustomerService() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/customer-service/faq")
    public ApiResponse<AiDTO> getCustomerServiceFAQ() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/search/flight")
     public ApiResponse<AiDTO> searchFlights(@RequestBody(required = false) Map<String, Object> payload) {
        Map<String, String> params = flightParams(payload);
        PageResponse<ProductDTO> result = flightSearchService.search(params);
        return ApiResponse.ok(aiResponse(flightSearchContent(params, result)));
    }

    @PostMapping("/search/hotel")
    public ApiResponse<AiDTO> searchHotels() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/search/travel")
    public ApiResponse<AiDTO> searchTravel() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/explain/refund")
    public ApiResponse<AiDTO> explainRefund(@RequestBody(required = false) Map<String, Object> payload) {
        String orderNo = text(payload, "orderNo", "order_no");
        if (orderNo.isBlank()) {
            return ApiResponse.ok(aiResponse("请提供 orderNo，系统会按航班起飞时间、订单金额和退改规则说明预计退款金额与手续费。"));
        }
        Map<String, Object> refund = flightSearchService.explainRefund(orderNo);
        return ApiResponse.ok(aiResponse(String.format(
                Locale.ROOT,
                "订单 %s 当前状态为%s。预计退款金额 %s，手续费 %s，规则：%s。%s",
                orderNo,
                refund.getOrDefault("statusText", "未知"),
                refund.getOrDefault("refundAmount", "0"),
                refund.getOrDefault("serviceFee", "0"),
                refund.getOrDefault("rule", "以航司实时规则为准"),
                refund.getOrDefault("explanation", "")
        )));
    }

    @PostMapping("/speech2text")
    public ApiResponse<AiDTO> speech2text() {
        return ApiResponse.ok(new AiDTO());
    }

    private AiDTO aiResponse(String content) {
        AiDTO dto = new AiDTO();
        dto.setContent(content);
        return dto;
    }

    private Map<String, String> flightParams(Map<String, Object> payload) {
        Map<String, String> params = new LinkedHashMap<>();
        copyParam(payload, params, "departureCity");
        copyParam(payload, params, "arrivalCity");
        copyParam(payload, params, "departureDate");
        copyParam(payload, params, "adultCount");
        copyParam(payload, params, "childCount");
        copyParam(payload, params, "cabin");
        copyParam(payload, params, "directOnly");
        copyParam(payload, params, "sort");

        String prompt = text(payload, "query", "prompt", "content", "text");
        if (!prompt.isBlank()) {
            inferCities(prompt, params);
            inferDate(prompt, params);
            inferPassengerCount(prompt, params);
            inferCabin(prompt, params);
            if (prompt.contains("直飞")) {
                params.putIfAbsent("directOnly", "true");
            }
        }
        params.putIfAbsent("adultCount", "1");
        params.putIfAbsent("sort", "price");
        params.putIfAbsent("page", "1");
        params.putIfAbsent("size", "5");
        return params;
    }

    private void copyParam(Map<String, Object> payload, Map<String, String> params, String key) {
        String value = text(payload, key);
        if (!value.isBlank()) {
            params.put(key, value);
        }
    }

    private void inferCities(String prompt, Map<String, String> params) {
        int fromIndex = prompt.indexOf("从");
        int toIndex = prompt.indexOf("到");
        if (fromIndex >= 0 && toIndex > fromIndex) {
            cityCode(prompt.substring(fromIndex + 1, toIndex)).ifPresent(code -> params.putIfAbsent("departureCity", code));
            cityCode(prompt.substring(toIndex + 1)).ifPresent(code -> params.putIfAbsent("arrivalCity", code));
        }
        for (Map.Entry<String, String> entry : CITY_CODES.entrySet()) {
            if (!prompt.contains(entry.getKey())) {
                continue;
            }
            if (!params.containsKey("departureCity")) {
                params.put("departureCity", entry.getValue());
            } else if (!params.containsKey("arrivalCity") && !entry.getValue().equals(params.get("departureCity"))) {
                params.put("arrivalCity", entry.getValue());
            }
        }
    }

    private java.util.Optional<String> cityCode(String text) {
        return CITY_CODES.entrySet()
                .stream()
                .filter(entry -> text.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private void inferDate(String prompt, Map<String, String> params) {
        java.util.regex.Matcher isoDate = java.util.regex.Pattern.compile("(20\\d{2}-\\d{2}-\\d{2})").matcher(prompt);
        if (isoDate.find()) {
            params.putIfAbsent("departureDate", isoDate.group(1));
            return;
        }
        java.util.regex.Matcher cnDate = java.util.regex.Pattern.compile("(\\d{1,2})月(\\d{1,2})[日号]?").matcher(prompt);
        if (cnDate.find()) {
            int month = Integer.parseInt(cnDate.group(1));
            int day = Integer.parseInt(cnDate.group(2));
            LocalDate candidate = LocalDate.of(LocalDate.now().getYear(), month, day);
            if (candidate.isBefore(LocalDate.now())) {
                candidate = candidate.plusYears(1);
            }
            params.putIfAbsent("departureDate", candidate.toString());
            return;
        }
        if (prompt.contains("明天")) {
            params.putIfAbsent("departureDate", LocalDate.now().plusDays(1).toString());
        } else if (prompt.contains("今天")) {
            params.putIfAbsent("departureDate", LocalDate.now().toString());
        }
    }

    private void inferPassengerCount(String prompt, Map<String, String> params) {
        java.util.regex.Matcher adult = java.util.regex.Pattern.compile("(\\d+)\\s*(个|位)?成人").matcher(prompt);
        if (adult.find()) {
            params.putIfAbsent("adultCount", adult.group(1));
        }
        java.util.regex.Matcher child = java.util.regex.Pattern.compile("(\\d+)\\s*(个|位)?儿童").matcher(prompt);
        if (child.find()) {
            params.putIfAbsent("childCount", child.group(1));
        }
    }

    private void inferCabin(String prompt, Map<String, String> params) {
        if (prompt.contains("商务舱")) {
            params.putIfAbsent("cabin", "BUSINESS");
        } else if (prompt.contains("头等舱")) {
            params.putIfAbsent("cabin", "FIRST");
        } else if (prompt.contains("经济舱")) {
            params.putIfAbsent("cabin", "ECONOMY");
        }
    }

    private String flightSearchContent(Map<String, String> params, PageResponse<ProductDTO> result) {
        List<ProductDTO> flights = result.getList();
        if (flights == null || flights.isEmpty()) {
            return "没有找到符合条件的航班，请调整城市、日期、舱位或人数后重试。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("为你找到 ")
                .append(result.getTotal())
                .append(" 个航班，按 ")
                .append(params.getOrDefault("sort", "price"))
                .append(" 排序：");
        for (ProductDTO flight : flights) {
            builder.append("\n")
                    .append(flight.getName())
                    .append("，￥")
                    .append(flight.getPrice())
                    .append("，余票 ")
                    .append(flight.getStock());
        }
        return builder.toString();
    }

    private String text(Map<String, Object> payload, String... keys) {
        if (payload == null || payload.isEmpty()) {
            return "";
        }
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }
}
