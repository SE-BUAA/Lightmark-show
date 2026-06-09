package top.ortus.lightmark.backend.exception;

/**
 * API 异常类，用于封装 API 请求过程中的错误信息
 */
public class ApiException extends RuntimeException {
    private final int code;

    /**
     * 构造函数
     * @param code 错误码
     * @param message 错误消息
     */
    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
}