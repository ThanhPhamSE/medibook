package com.medibook.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private boolean error;
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int status, boolean error, String message, T data) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.data = data;
    }

    // SUCCESS
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, false, "OK", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, false, message, data);
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(status, false, message, data);
    }

    // ERROR
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, true, message, null);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, true, message, null);
    }

    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return new ApiResponse<>(status, true, message, data);
    }

    public int getStatus() {
        return status;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
