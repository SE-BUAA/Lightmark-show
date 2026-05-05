package top.ortus.timemark.backend.common;

public class ApiResponse<T> {
    private int code;
    private String errorMsg;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int code, String errorMsg, T data) {
        this.code = code;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "", data);
    }

    public static <T> ApiResponse<T> error(int code, String errorMsg) {
        return new ApiResponse<>(code, errorMsg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

