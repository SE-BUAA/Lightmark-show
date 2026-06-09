package top.ortus.lightmark.backend.dto.chat;

public class StreamChatRequest {
    private String sessionId;
    private String message;

    public StreamChatRequest() {}

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

