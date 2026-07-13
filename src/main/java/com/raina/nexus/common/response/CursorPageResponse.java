package com.raina.nexus.common.response;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        Long nextCursor,
        boolean hasNext
) {
}
