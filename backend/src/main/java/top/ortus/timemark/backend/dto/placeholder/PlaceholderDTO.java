package top.ortus.timemark.backend.dto.placeholder;

public class PlaceholderDTO {
    private String module;
    private String message;

    public PlaceholderDTO() {
    }

    public PlaceholderDTO(String module, String message) {
        this.module = module;
        this.message = message;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

