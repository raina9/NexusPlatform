package com.raina.nexus.common.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
}