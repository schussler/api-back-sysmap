package com.sysmap.hubapi.dto.response;

import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String cpf,
        String avatar,
        int xp,
        int level,
        List<AchievementResponse> achievements
) {}
