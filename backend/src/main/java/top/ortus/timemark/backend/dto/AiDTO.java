package top.ortus.timemark.backend.dto;

public class AiDTO {
    public String content;
    public String model;

    public AiDTO() {
        content = "We haven't got any content yet!";
        model = "deepseek-v4-flash";
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
}
