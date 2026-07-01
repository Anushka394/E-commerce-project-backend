package com.ecommerce.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Generic wrapper for simple success/error messages.
 * Used when there's no entity to return (e.g. delete operations).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private int status;
    private String message;
    private String timestamp;

    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
