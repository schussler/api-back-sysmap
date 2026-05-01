package com.sysmap.hubapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID activityId,
        UUID userId,
        boolean approved,
        LocalDateTime confirmedAt
) {}
