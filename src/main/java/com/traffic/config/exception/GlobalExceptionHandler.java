package com.traffic.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 统一错误响应格式
     */
    public static class ErrorResponse {
        private final String timestamp;
        private final int status;
        private final String error;
        private final String message;
        private final String path;
        private final String errorCode;
        private final Object details;

        public ErrorResponse(HttpStatus status, String message, String path) {
            this(status, message, path, null, null);
        }

        public ErrorResponse(HttpStatus status, String message, String path, String errorCode) {
            this(status, message, path, errorCode, null);
        }

        public ErrorResponse(HttpStatus status, String message, String path, String errorCode, Object details) {
            this.timestamp = LocalDateTime.now().toString();
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
            this.path = path;
            this.errorCode = errorCode;
            this.details = details;
        }

        // Getters
        public String getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public String getErrorCode() { return errorCode; }
        public Object getDetails() { return details; }
    }

    /**
     * 处理配置相关异常
     */
    @ExceptionHandler(ConfigException.class)
    public ResponseEntity<ErrorResponse> handleConfigException(ConfigException ex, WebRequest request) {
        log.error("配置异常: {}", ex.getMessage(), ex);

        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        ErrorResponse errorResponse = new ErrorResponse(
                status,
                ex.getErrorMessage(),
                getPath(request),
                ex.getErrorCode(),
                ex.getDetails()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("参数验证失败: {}", ex.getMessage());

        List<Map<String, String>> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            Map<String, String> errorDetail = new HashMap<>();
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorDetail.put("field", fieldName);
            errorDetail.put("message", errorMessage);
            errors.add(errorDetail);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "参数验证失败",
                getPath(request),
                "VALIDATION_ERROR",
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        log.warn("数据绑定异常: {}", ex.getMessage());

        List<Map<String, String>> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            Map<String, String> errorDetail = new HashMap<>();
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorDetail.put("field", fieldName);
            errorDetail.put("message", errorMessage);
            errors.add(errorDetail);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "数据绑定失败",
                getPath(request),
                "BIND_ERROR",
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        log.warn("约束违反异常: {}", ex.getMessage());

        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    Map<String, String> errorDetail = new HashMap<>();
                    errorDetail.put("field", violation.getPropertyPath().toString());
                    errorDetail.put("message", violation.getMessage());
                    errorDetail.put("invalidValue", String.valueOf(violation.getInvalidValue()));
                    return errorDetail;
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "约束违反",
                getPath(request),
                "CONSTRAINT_VIOLATION",
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("缺少请求参数: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "缺少必需的请求参数: " + ex.getParameterName(),
                getPath(request),
                "MISSING_PARAMETER"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("参数类型不匹配: {}", ex.getMessage());

        String message = String.format("参数 '%s' 的值 '%s' 不能转换为 %s 类型",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                getPath(request),
                "TYPE_MISMATCH"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("HTTP消息不可读: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "请求体格式错误或不可读",
                getPath(request),
                "MESSAGE_NOT_READABLE"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理不支持的HTTP方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("不支持的HTTP方法: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "不支持的HTTP方法: " + ex.getMethod(),
                getPath(request),
                "METHOD_NOT_ALLOWED"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 处理不支持的媒体类型异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        log.warn("不支持的媒体类型: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "不支持的媒体类型: " + ex.getContentType(),
                getPath(request),
                "MEDIA_TYPE_NOT_SUPPORTED"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        log.warn("未找到处理器: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                "请求的资源不存在: " + ex.getRequestURL(),
                getPath(request),
                "NOT_FOUND"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("非法参数异常: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "参数错误: " + ex.getMessage(),
                getPath(request),
                "ILLEGAL_ARGUMENT"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex, WebRequest request) {
        log.error("空指针异常", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "服务器内部错误",
                getPath(request),
                "NULL_POINTER"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        log.error("未处理的异常: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "服务器内部错误: " + ex.getMessage(),
                getPath(request),
                "INTERNAL_ERROR"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 根据错误码确定HTTP状态码
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "CONFIG_FILE_NOT_FOUND", "SEGMENT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFIG_VALIDATION_ERROR", "SEGMENT_ALREADY_EXISTS" -> HttpStatus.BAD_REQUEST;
            case "CONFIG_LOCKED", "CONFIG_VERSION_CONFLICT" -> HttpStatus.CONFLICT;
            case "CONFIG_PARSE_ERROR", "CONFIG_SAVE_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * 获取请求路径
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}