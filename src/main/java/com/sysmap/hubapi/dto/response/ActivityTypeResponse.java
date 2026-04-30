package com.sysmap.hubapi.dto.response;

import java.util.UUID;

public record ActivityTypeResponse(UUID id, String name, String description, String image) {}
