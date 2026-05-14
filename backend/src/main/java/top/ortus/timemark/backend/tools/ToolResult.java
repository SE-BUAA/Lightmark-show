package top.ortus.timemark.backend.tools;

import java.util.Map;

public class ToolResult {
    private boolean success;
    private String message;
    private Map<String, Object> data;

    public ToolResult() {}

    public ToolResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ToolResult(boolean success, String message, Map<String, Object> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

