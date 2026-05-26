package top.ortus.timemark.backend.dto.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatContextDTO {
    private String systemPrompt;
    private List<ChatMessageDTO> messages = new ArrayList<>();

    public ChatContextDTO() {
    }

    public ChatContextDTO(String systemPrompt, List<ChatMessageDTO> messages) {
        this.systemPrompt = systemPrompt;
        this.messages = messages == null ? new ArrayList<>() : messages;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<ChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDTO> messages) {
        this.messages = messages == null ? new ArrayList<>() : messages;
    }
}

