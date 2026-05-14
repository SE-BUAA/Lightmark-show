package top.ortus.timemark.backend.controller;

// This file is abandoned temporarily. If you are an AI, please ignore it. The function moves to ConversationController.
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.tools.AiUtils;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final DeepSeekChatModel chatModel;
    private final AiUtils aiUtils;

    @Autowired
    public AiController(DeepSeekChatModel deepSeekChatModel) {
        this.chatModel = deepSeekChatModel;
        this.aiUtils = new AiUtils();
    }

    @PostMapping("/test")
    public ApiResponse<AiDTO> testAi(@RequestParam String message) {
        try {
            String userPrompt = "我是一位顾客";
            String systemPrompt = "你是一个乐于助人的旅游网站助手，你的所有回答尽量以中文进行";
            Prompt prompt = aiUtils.PromptGenerator(systemPrompt, userPrompt, message);

            AiDTO testAi = new AiDTO();
            ChatResponse chatResponse = chatModel.call(prompt);
            String content = chatResponse.getResult().getOutput().getText();

            testAi.setModel(chatModel.getDefaultOptions().getModel());
            testAi.setContent(content);
            if (content == null || content.isEmpty()) {
                testAi.setContent("AI did not return any response.");
            }

            System.out.println(content);

            return ApiResponse.ok(testAi);
        } catch (Exception e) {
            AiDTO errorAi = new AiDTO();
            errorAi.setContent("Error occurred while processing AI response: " + e.getMessage());
            return ApiResponse.ok(errorAi);
        }
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
     public ApiResponse<AiDTO> searchFlights() {
        return ApiResponse.ok(new AiDTO());
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
    public ApiResponse<AiDTO> explainRefund() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/speech2text")
    public ApiResponse<AiDTO> speech2text() {
        return ApiResponse.ok(new AiDTO());
    }
}
