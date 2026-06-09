package top.ortus.lightmark.backend.dto;

import org.springframework.beans.factory.annotation.Value;

public class AiDTO {
    private String content;
    @Value("${spring.ai.deepseek.chat.options.model}") private String model;

    public AiDTO() {
        this.content = "We haven't got any content yet!";
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModelName() {
        return model;
    }
}
