package com.sysmap.hubapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        String title,
        String description,
        String type,
        String image,
        String confirmationCode,
        long participantCount,
        AddressResponse address,
        LocalDateTime scheduledDate,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        Boolean isPrivate,
        CreatorResponse creator,
        String userSubscriptionStatus
) {}
