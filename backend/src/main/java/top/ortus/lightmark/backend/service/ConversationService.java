package top.ortus.lightmark.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.ortus.lightmark.backend.dto.AiDTO;
import top.ortus.lightmark.backend.dto.chat.ChatContextDTO;

@Service
public class ConversationService {

    private final DeepSeekNativeChatService nativeChatService;

    public ConversationService(DeepSeekNativeChatService nativeChatService) {
        this.nativeChatService = nativeChatService;
    }

    /**
     * 发送消息并保持上下文
     */
    public AiDTO chat(String sessionId, String userMessage, String systemPrompt) {
        return nativeChatService.chat(sessionId, userMessage, systemPrompt);
    }

    public ChatContextDTO getContext(String sessionId) {
        return nativeChatService.getContext(sessionId);
    }

    public boolean resetContext(String sessionId) {
        return nativeChatService.resetContext(sessionId);
    }

    /**
     * 区域补全：只为给定区域生成替换内容（保留前后文提示）
     */
    public AiDTO regionComplete(String sessionId, String regionText, String surroundingText) {
        return nativeChatService.chat(
                sessionId,
                "Region to replace:\n" + (regionText == null ? "" : regionText) + "\nContext:\n" + (surroundingText == null ? "" : surroundingText),
                "请仅返回对指定区域的替换文本，不要输出多余说明。返回结果只包含替换后的区域内容。"
        );
    }

    /**
     * 流式聊天（SSE）
     */
    public void streamChat(String sessionId, String userMessage, SseEmitter emitter) {
        nativeChatService.streamChat(sessionId, userMessage, emitter);
    }
}
