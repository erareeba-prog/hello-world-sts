package com.example.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private int status;
    private LocalDateTime timestamp;

    // ✅ Success response with data
    public static <T> ApiResponse<T> success(
            T data, String message, int status) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success   = true;
        r.message   = message;
        r.data      = data;
        r.status    = status;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success", 200);
    }

    public static <T> ApiResponse<T> success(
            T data, String message) {
        return success(data, message, 200);
    }

    // ✅ Error response
    public static <T> ApiResponse<T> error(
            String error, int status) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success   = false;
        r.error     = error;
        r.status    = status;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    // Getters
    public boolean isSuccess()        { return success; }
    public String getMessage()        { return message; }
    public T getData()                { return data; }
    public String getError()          { return error; }
    public int getStatus()            { return status; }
    public LocalDateTime getTimestamp(){ return timestamp; }
}