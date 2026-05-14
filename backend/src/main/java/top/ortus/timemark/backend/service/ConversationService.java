package top.ortus.timemark.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.tools.UpdateEmailTool;
import top.ortus.timemark.backend.tools.UpdateNicknameTool;
import top.ortus.timemark.backend.tools.WebSearchTool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {
    // 存储会话ID -> 消息列表（线程安全）
    private final ConcurrentHashMap<String, List<Message>> conversations = new ConcurrentHashMap<>();

    private final DeepSeekChatModel chatModel;
    private final ChatClient chatClient;
    private final DeepSeekChatOptions defaultOptions;

    public ConversationService(DeepSeekChatModel chatModel,
                               ChatClient.Builder chatClientBuilder,
                               UpdateNicknameTool updateNicknameTool,
                               UpdateEmailTool updateEmailTool,
                               WebSearchTool webSearchTool) {
        this.chatModel = chatModel;
        this.defaultOptions = new DeepSeekChatOptions.Builder()
                .temperature(0.7)
                .internalToolExecutionEnabled(true)
                .build();
        this.chatClient = chatClientBuilder
                .defaultTools(updateNicknameTool, updateEmailTool, webSearchTool)
                .build();
    }

    /**
     * 发送消息并保持上下文
     * @param sessionId 会话唯一标识（如用户ID、聊天室ID）
     * @param userMessage 用户输入
     * @return AI回复内容
     */
    public AiDTO chat(String sessionId, String userMessage) {
        // 1. 获取或创建对话历史
        List<Message> history = conversations.computeIfAbsent(sessionId,
                id -> new ArrayList<>());

        // 2. 加入当前用户消息
        history.add(new UserMessage(userMessage));

        // 3. 调用模型（传入完整历史）
        String aiMessage = chatClient.prompt()
                .messages(history)
                .options(defaultOptions)
                .call()
                .content();

        // 4. 将AI回复也存入历史
        if (aiMessage != null) {
            history.add(new AssistantMessage(aiMessage));
        }

        AiDTO aiDTO = new AiDTO();
        aiDTO.setContent(aiMessage);
        aiDTO.setModel(chatModel.getDefaultOptions().getModel());

        return aiDTO;
    }

    // 可选：清理历史（如超过限制则移除最早的消息）
    private void trimHistory(List<Message> history, int maxSize) {
        while (history.size() > maxSize) {
            history.remove(0);
        }
    }

    /**
     * 区域补全：只为给定区域生成替换内容（保留前后文提示）
     */
    public AiDTO regionComplete(String sessionId, String regionText, String surroundingText) {
        List<Message> history = conversations.computeIfAbsent(sessionId,
                id -> new ArrayList<>());

        // 构造一个专门用于区域补全的提示，保持前后文但突出要求只返回替换文本
        String systemPrompt = "请仅返回对指定区域的替换文本，不要输出多余说明。返回结果只包含替换后的区域内容。";
        String userPrompt = "Region to replace:\n" + regionText + "\nContext:\n" + surroundingText;

        String aiMessage = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(defaultOptions)
                .call()
                .content();

        if (aiMessage != null) {
            history.add(new AssistantMessage(aiMessage));
        }

        AiDTO aiDTO = new AiDTO();
        aiDTO.setContent(aiMessage == null ? "" : aiMessage);
        aiDTO.setModel(chatModel.getDefaultOptions().getModel());
        return aiDTO;
    }

    /**
     * 流式聊天（简单实现）
     * 说明：当前实现把完整回复拆成分片并通过 SSE 发送。未来可替换为模型原生流式接口。
     */
    public void streamChat(String sessionId, String userMessage, SseEmitter emitter) {
        // 在后台线程执行模型调用并推送事件
        new Thread(() -> {
            try {
                List<Message> history = conversations.computeIfAbsent(sessionId,
                        id -> new ArrayList<>());

                history.add(new UserMessage(userMessage));

                String aiMessage = chatClient.prompt()
                        .messages(history)
                        .options(defaultOptions)
                        .call()
                        .content();

                // 简单分片，每100字符为一片
                if (aiMessage != null) {
                    int chunkSize = 100;
                    int len = aiMessage.length();
                    for (int start = 0; start < len; start += chunkSize) {
                        int end = Math.min(len, start + chunkSize);
                        String piece = aiMessage.substring(start, end);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("partial")
                                    .data(piece));
                        } catch (Exception e) {
                            // 客户端断开或发送失败，终止
                            break;
                        }
                        // 可选：短暂睡眠以模拟实时流出
                        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                    }

                    // 发送完成事件
                    emitter.send(SseEmitter.event().name("complete").data("__complete__"));
                    history.add(new AssistantMessage(aiMessage));
                } else {
                    emitter.send(SseEmitter.event().name("complete").data(""));
                }

                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("AI error: " + ex.getMessage()));
                } catch (Exception ignore) {}
                emitter.completeWithError(ex);
            }
        }).start();
    }
}

