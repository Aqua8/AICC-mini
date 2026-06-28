package com.aicc.backend.dto.common;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        Long nextCursor,
        boolean hasNext
) {
}
