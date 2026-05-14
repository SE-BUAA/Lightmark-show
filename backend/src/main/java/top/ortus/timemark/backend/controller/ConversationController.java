package top.ortus.timemark.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.dto.chat.ChatRequest;
import top.ortus.timemark.backend.dto.chat.RegionCompleteRequest;
import top.ortus.timemark.backend.dto.chat.StreamChatRequest;
import top.ortus.timemark.backend.service.ConversationService;

@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // 非流式聊天
    @PostMapping("")
    public ApiResponse<AiDTO> chat(@RequestBody ChatRequest req) {
        AiDTO aiDTO = conversationService.chat(req.getSessionId(), req.getMessage());
        return ApiResponse.ok(aiDTO);
    }

    // 区域补全（非流式）
    @PostMapping("/region/complete")
    public ApiResponse<AiDTO> regionComplete(@RequestBody RegionCompleteRequest req) {
        AiDTO aiDTO = conversationService.regionComplete(req.getSessionId(), req.getRegionText(), req.getSurroundingText());
        return ApiResponse.ok(aiDTO);
    }

    // 简易流式接口（SSE）
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody StreamChatRequest req) {
        SseEmitter emitter = new SseEmitter(0L);
        conversationService.streamChat(req.getSessionId(), req.getMessage(), emitter);
        return emitter;
    }
}

