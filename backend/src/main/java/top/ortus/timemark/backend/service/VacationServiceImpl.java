package top.ortus.timemark.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dao.ProductMapper;
import top.ortus.timemark.backend.dto.module.VacationAiDetailResponse;
import top.ortus.timemark.backend.dto.module.VacationOptionsResponse;
import top.ortus.timemark.backend.dto.module.VacationSearchRequest;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.tools.UpdateEmailTool;
import top.ortus.timemark.backend.tools.UpdateNicknameTool;
import top.ortus.timemark.backend.tools.WebSearchTool;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class VacationServiceImpl implements VacationService{
    @Autowired
    private ProductMapper productMapper;

    private final ChatClient chatClient;
    private final DeepSeekChatModel chatModel;
    private final ConversationService conversationService;
    private final DeepSeekChatOptions aiOptions = new DeepSeekChatOptions.Builder()
        .temperature(0.8)
        .internalToolExecutionEnabled(true)
        .build();

    public VacationServiceImpl(DeepSeekChatModel chatModel,
                               ChatClient.Builder chatClientBuilder,
                               UpdateNicknameTool updateNicknameTool,
                               UpdateEmailTool updateEmailTool,
                               WebSearchTool webSearchTool,
                               ConversationService conversationService) {
        this.chatModel = chatModel;
        this.conversationService = conversationService;
        this.chatClient = chatClientBuilder
            .defaultTools(updateNicknameTool, updateEmailTool, webSearchTool)
            .build();
    }

    @Override
    public List<Product> search(VacationSearchRequest request) {
        List<Product> vacations = selectActiveVacations();
        if (request == null) {
            return vacations;
        }
        return vacations.stream()
            .filter(product -> matchesExtra(product, "destination", request.getDestination()))
            .filter(product -> matchesExtra(product, "depart_city", request.getDepartCity()))
            .filter(product -> matchesExtra(product, "date", request.getDate()))
            .filter(product -> matchesDays(product, request.getMinDays(), request.getMaxDays()))
            .filter(product -> matchesPrice(product, request.getMinPrice(), request.getMaxPrice()))
            .filter(product -> matchesAnyTag(product, request.getTags()))
            .toList();
    }

    @Override
    public Product searchById(Integer productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public VacationOptionsResponse options() {
        List<Product> vacations = selectActiveVacations();
        List<String> tags = vacations.stream()
            .flatMap(product -> product.getCategoryTags() == null ? java.util.stream.Stream.empty() : product.getCategoryTags().stream())
            .filter(tag -> tag != null && !tag.isBlank())
            .distinct()
            .sorted(Comparator.naturalOrder())
            .toList();
        return new VacationOptionsResponse(
            distinctExtraValues(vacations, "destination"),
            distinctExtraValues(vacations, "depart_city"),
            distinctExtraValues(vacations, "date"),
            tags
        );
    }

    @Override
    public VacationAiDetailResponse generateAiDetail(Integer productId) {
        Product product = searchById(productId);
        if (product == null || !"VACATION".equals(product.getProductType())) {
            throw new IllegalArgumentException("度假产品不存在");
        }
        String itinerary = buildItinerary(product);
        String tags = product.getCategoryTags() == null ? "" : String.join("、", product.getCategoryTags());
        String prompt = "请你生成该度假产品的详情介绍文案，行程为" + itinerary + "，标签为" + tags + "，100字左右，语气轻松活泼，可以包含emoji";
        if (product.getId() != null) {
            try {
                String sessionId = "vacation-detail-" + product.getId() + "-" + System.nanoTime();
                AiDTO aiDTO = conversationService.chat(
                    sessionId,
                    prompt,
                    "你是一个旅游度假产品文案助手，请用中文输出，直接给出产品详情介绍文案，不要调用工具。"
                );
                String content = aiDTO == null ? "" : aiDTO.getContent();
                return new VacationAiDetailResponse(product.getId(), (content == null || content.isBlank()) ? fallbackDetail(product, itinerary, tags) : content);
            } catch (Exception ex) {
                return new VacationAiDetailResponse(product.getId(), fallbackDetail(product, itinerary, tags));
            }
        }
        Prompt aiPrompt = new Prompt(
            List.of(
                new SystemMessage("你是一个旅游度假产品文案助手，请用中文输出，直接给出产品详情介绍文案。"),
                new UserMessage(prompt)
            ),
            aiOptions
        );
        ChatResponse response = chatModel.call(aiPrompt);
        String content = response == null || response.getResult() == null || response.getResult().getOutput() == null
            ? ""
            : response.getResult().getOutput().getText();
        return new VacationAiDetailResponse(product.getId(), content == null ? "" : content);
    }

    private List<Product> selectActiveVacations() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductType, "VACATION")
            .eq(Product::getStatus, 1)
            .orderByAsc(Product::getId);
        return productMapper.selectList(queryWrapper);
    }

    private boolean matchesExtra(Product product, String key, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        Object actual = product.getExtra() == null ? null : product.getExtra().get(key);
        return expected.equals(String.valueOf(actual));
    }

    private String buildItinerary(Product product) {
        Object departCity = product.getExtra() == null ? null : product.getExtra().get("depart_city");
        Object destination = product.getExtra() == null ? null : product.getExtra().get("destination");
        Object date = product.getExtra() == null ? null : product.getExtra().get("date");
        Object days = product.getExtra() == null ? null : product.getExtra().get("days");
        Object hotelLevel = product.getExtra() == null ? null : product.getExtra().get("hotel_level");
        Object summary = product.getExtra() == null ? null : product.getExtra().get("summary");
        return product.getName()
            + "，" + valueOrBlank(departCity) + "出发"
            + "，前往" + valueOrBlank(destination)
            + "，日期" + valueOrBlank(date)
            + "，" + valueOrBlank(days) + "天"
            + "，酒店" + valueOrBlank(hotelLevel)
            + "，亮点：" + valueOrBlank(summary);
    }

    private String fallbackDetail(Product product, String itinerary, String tags) {
        return product.getName() + "来啦！" + itinerary + "。"
            + (tags == null || tags.isBlank() ? "" : "适合喜欢" + tags + "的你，")
            + "轻松安排吃住玩，省心出发，快乐度假～";
    }

    private String valueOrBlank(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean matchesDays(Product product, Integer minDays, Integer maxDays) {
        if (minDays == null && maxDays == null) {
            return true;
        }
        Object value = product.getExtra() == null ? null : product.getExtra().get("days");
        if (value == null) {
            return false;
        }
        int days = Integer.parseInt(String.valueOf(value));
        return (minDays == null || days >= minDays) && (maxDays == null || days <= maxDays);
    }

    private boolean matchesPrice(Product product, Double minPrice, Double maxPrice) {
        double price = product.getPrice() == null ? 0 : product.getPrice();
        return (minPrice == null || price >= minPrice) && (maxPrice == null || price <= maxPrice);
    }

    private boolean matchesAnyTag(Product product, List<String> expectedTags) {
        if (expectedTags == null || expectedTags.isEmpty()) {
            return true;
        }
        List<String> tags = product.getCategoryTags();
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        return expectedTags.stream().anyMatch(tags::contains);
    }

    private List<String> distinctExtraValues(List<Product> products, String key) {
        return products.stream()
            .map(product -> product.getExtra() == null ? null : product.getExtra().get(key))
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .filter(value -> !value.isBlank())
            .distinct()
            .sorted(Comparator.naturalOrder())
            .toList();
    }
}
