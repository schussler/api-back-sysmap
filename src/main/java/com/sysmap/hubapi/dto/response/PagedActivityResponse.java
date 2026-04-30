package com.sysmap.hubapi.dto.response;

import java.util.List;

public record PagedActivityResponse(
        int page,
        int pageSize,
        long totalActivities,
        int totalPages,
        int previous,
        int next,
        List<ActivityResponse> activities
) {}
