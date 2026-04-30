package com.sysmap.hubapi.dto.response;

import java.util.UUID;

public record PreferenceResponse(UUID typeId, String typeName, String typeDescription) {}
