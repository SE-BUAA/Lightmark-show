package top.ortus.timemark.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import top.ortus.timemark.backend.common.ApiResponse;

/**
 * 全局异常处理器，统一处理应用程序中的各种异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 API 异常
     * @param ex API 异常
     * @return 错误响应
     */
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleApiException(ApiException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理非法参数异常
     * @param ex 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    /**
     * 处理参数验证异常和消息不可读异常
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(Exception ex) {
        return ApiResponse.error(400, "invalid request");
    }

    /**
     * 处理静态资源找不到异常（例如 favicon.ico）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoResourceFound(NoResourceFoundException ex) {
        return ApiResponse.error(404, "resource not found");
    }

    /**
     * 处理通用异常
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return ApiResponse.error(500, ex.getMessage() != null ? ex.getMessage() : "internal error");
    }
}