// ==================== 统一API响应类 ====================

/**
 * 统一API响应包装类
 *
 * @author System
 * @version 1.0.0
 */
package com.traffic.config.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误详情（仅在失败时返回）
     */
    private Object errorDetails;

    // ==================== 响应码常量 ====================

    /**
     * 响应码枚举
     */
    public enum ResponseCode {
        // 成功响应
        SUCCESS(200, "操作成功"),
        CREATED(201, "创建成功"),
        ACCEPTED(202, "请求已接受"),
        NO_CONTENT(204, "无内容"),

        // 客户端错误
        BAD_REQUEST(400, "请求参数错误"),
        UNAUTHORIZED(401, "未授权"),
        FORBIDDEN(403, "禁止访问"),
        NOT_FOUND(404, "资源不存在"),
        METHOD_NOT_ALLOWED(405, "方法不允许"),
        CONFLICT(409, "资源冲突"),
        VALIDATION_ERROR(422, "参数验证失败"),

        // 服务器错误
        INTERNAL_ERROR(500, "服务器内部错误"),
        BAD_GATEWAY(502, "网关错误"),
        SERVICE_UNAVAILABLE(503, "服务不可用"),
        GATEWAY_TIMEOUT(504, "网关超时"),

        // 业务错误码（自定义）
        CONFIG_ERROR(1001, "配置错误"),
        FILE_ERROR(1002, "文件操作错误"),
        VALIDATION_FAILED(1003, "数据验证失败"),
        SEGMENT_NOT_FOUND(1004, "路段不存在"),
        SEGMENT_EXISTS(1005, "路段已存在"),
        CONFIG_LOCKED(1006, "配置被锁定"),
        VERSION_CONFLICT(1007, "版本冲突");

        private final int code;
        private final String message;

        ResponseCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数
     *
     * @param code 响应码
     * @param message 响应消息
     * @param data 响应数据
     */
    public ApiResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = isSuccessCode(code);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码枚举
     * @param data 响应数据
     */
    public ApiResponse(ResponseCode responseCode, T data) {
        this(responseCode.getCode(), responseCode.getMessage(), data);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码枚举
     */
    public ApiResponse(ResponseCode responseCode) {
        this(responseCode, null);
    }

    // ==================== 成功响应静态方法 ====================

    /**
     * 成功响应（无数据）
     *
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResponseCode.SUCCESS);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS, data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 响应消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 成功响应（自定义消息和数据）
     *
     * @param message 响应消息
     * @param data 响应数据
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 创建成功响应
     *
     * @param data 创建的数据
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(ResponseCode.CREATED, data);
    }

    /**
     * 接受响应（异步处理）
     *
     * @param message 响应消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> accepted(String message) {
        return new ApiResponse<>(ResponseCode.ACCEPTED.getCode(), message, null);
    }

    /**
     * 无内容响应
     *
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> noContent() {
        return new ApiResponse<>(ResponseCode.NO_CONTENT);
    }

    // ==================== 失败响应静态方法 ====================

    /**
     * 失败响应
     *
     * @param responseCode 响应码枚举
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        ApiResponse<T> response = new ApiResponse<>(responseCode);
        response.setSuccess(false);
        return response;
    }

    /**
     * 失败响应（自定义消息）
     *
     * @param responseCode 响应码枚举
     * @param message 自定义消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
        ApiResponse<T> response = new ApiResponse<>(responseCode.getCode(), message, null);
        response.setSuccess(false);
        return response;
    }

    /**
     * 失败响应（带错误详情）
     *
     * @param responseCode 响应码枚举
     * @param message 响应消息
     * @param errorDetails 错误详情
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message, Object errorDetails) {
        ApiResponse<T> response = new ApiResponse<>(responseCode.getCode(), message, null);
        response.setSuccess(false);
        response.setErrorDetails(errorDetails);
        return response;
    }

    /**
     * 自定义错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>(code, message, null);
        response.setSuccess(false);
        return response;
    }

    /**
     * 参数错误响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(ResponseCode.BAD_REQUEST, message);
    }

    /**
     * 参数验证失败响应
     *
     * @param message 错误消息
     * @param validationErrors 验证错误详情
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> validationError(String message, Object validationErrors) {
        return error(ResponseCode.VALIDATION_ERROR, message, validationErrors);
    }

    /**
     * 资源不存在响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(ResponseCode.NOT_FOUND, message);
    }

    /**
     * 未授权响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(ResponseCode.UNAUTHORIZED, message);
    }

    /**
     * 禁止访问响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(ResponseCode.FORBIDDEN, message);
    }

    /**
     * 资源冲突响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> conflict(String message) {
        return error(ResponseCode.CONFLICT, message);
    }

    /**
     * 服务器内部错误响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return error(ResponseCode.INTERNAL_ERROR, message);
    }

    /**
     * 服务不可用响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> serviceUnavailable(String message) {
        return error(ResponseCode.SERVICE_UNAVAILABLE, message);
    }

    // ==================== 业务相关响应方法 ====================

    /**
     * 配置错误响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> configError(String message) {
        return error(ResponseCode.CONFIG_ERROR, message);
    }

    /**
     * 配置错误响应（带详情）
     *
     * @param message 错误消息
     * @param errorDetails 错误详情
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> configError(String message, Object errorDetails) {
        return error(ResponseCode.CONFIG_ERROR, message, errorDetails);
    }

    /**
     * 文件错误响应
     *
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> fileError(String message) {
        return error(ResponseCode.FILE_ERROR, message);
    }

    /**
     * 路段不存在响应
     *
     * @param sigid 信号灯ID
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> segmentNotFound(String sigid) {
        return error(ResponseCode.SEGMENT_NOT_FOUND, "路段不存在: " + sigid);
    }

    /**
     * 路段已存在响应
     *
     * @param sigid 信号灯ID
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> segmentExists(String sigid) {
        return error(ResponseCode.SEGMENT_EXISTS, "路段已存在: " + sigid);
    }

    /**
     * 配置被锁定响应
     *
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> configLocked() {
        return error(ResponseCode.CONFIG_LOCKED, "配置文件被其他进程锁定，请稍后重试");
    }

    /**
     * 版本冲突响应
     *
     * @param expectedVersion 期望版本
     * @param actualVersion 实际版本
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> versionConflict(long expectedVersion, long actualVersion) {
        String message = String.format("配置版本冲突，期望版本: %d，实际版本: %d", expectedVersion, actualVersion);
        Object details = new java.util.HashMap<String, Long>() {{
            put("expectedVersion", expectedVersion);
            put("actualVersion", actualVersion);
        }};
        return error(ResponseCode.VERSION_CONFLICT, message, details);
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断响应码是否为成功
     *
     * @param code 响应码
     * @return 是否成功
     */
    private boolean isSuccessCode(Integer code) {
        return code != null && code >= 200 && code < 300;
    }

    /**
     * 设置请求ID
     *
     * @param requestId 请求ID
     * @return 当前实例（用于链式调用）
     */
    public ApiResponse<T> requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * 设置错误详情
     *
     * @param errorDetails 错误详情
     * @return 当前实例（用于链式调用）
     */
    public ApiResponse<T> errorDetails(Object errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }

    /**
     * 判断当前响应是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.success);
    }

    /**
     * 判断当前响应是否失败
     *
     * @return 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }

    // ==================== 重写toString方法 ====================

    @Override
    public String toString() {
        return String.format("ApiResponse{code=%d, message='%s', success=%s, timestamp=%s}",
                code, message, success, timestamp);
    }

    // ==================== 分页响应类 ====================

    /**
     * 分页响应数据包装类
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PageData<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 数据列表
         */
        private java.util.List<T> items;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 当前页码
         */
        private Integer page;

        /**
         * 每页大小
         */
        private Integer pageSize;

        /**
         * 总页数
         */
        private Integer totalPages;

        /**
         * 是否有下一页
         */
        private Boolean hasNext;

        /**
         * 是否有上一页
         */
        private Boolean hasPrevious;

        /**
         * 构造函数
         */
        public PageData() {}

        /**
         * 构造函数
         *
         * @param items 数据列表
         * @param total 总记录数
         * @param page 当前页码
         * @param pageSize 每页大小
         */
        public PageData(java.util.List<T> items, Long total, Integer page, Integer pageSize) {
            this.items = items;
            this.total = total;
            this.page = page;
            this.pageSize = pageSize;
            this.totalPages = calculateTotalPages(total, pageSize);
            this.hasNext = page < totalPages;
            this.hasPrevious = page > 1;
        }

        /**
         * 计算总页数
         */
        private Integer calculateTotalPages(Long total, Integer pageSize) {
            if (total == null || total <= 0 || pageSize == null || pageSize <= 0) {
                return 0;
            }
            return (int) Math.ceil((double) total / pageSize);
        }
    }

    /**
     * 分页成功响应
     *
     * @param items 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<PageData<T>> successPage(java.util.List<T> items, Long total, Integer page, Integer pageSize) {
        PageData<T> pageData = new PageData<>(items, total, page, pageSize);
        return success(pageData);
    }

    /**
     * 分页成功响应（自定义消息）
     *
     * @param message 响应消息
     * @param items 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<PageData<T>> successPage(String message, java.util.List<T> items, Long total, Integer page, Integer pageSize) {
        PageData<T> pageData = new PageData<>(items, total, page, pageSize);
        return success(message, pageData);
    }
}