package top.ortus.lightmark.backend.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * API 响应包装类，用于统一 API 响应格式
 * @param <T> 响应数据的类型
 */
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 创建成功的响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功的 API 响应
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * 创建错误的响应
     * @param code 错误码
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return 错误的 API 响应
     */
    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * Backward-compatible alias.
     */
    @JsonIgnore
    public String getErrorMsg() {
        return msg;
    }

    /**
     * Backward-compatible alias.
     */
    public void setErrorMsg(String errorMsg) {
        this.msg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}