package com.sysmap.hubapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantResponse(
        UUID id,
        UUID userId,
        String name,
        String avatar,
        String subscriptionStatus,
        LocalDateTime confirmedAt
) {}
